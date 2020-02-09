package Base;

import DTO.Protocol;
import DTO.Statistic;
import Exceptions.LackOfInformationException;
import FileCreators.CSVCreator;
import FileCreators.ExcelCreator;
import FileCreators.HTMLCreator;
import Mail.MailService;
import Mail.SendOverMailConfig;
import Pools.SqlConnectionPool;
import Services.CurrentPath;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.nlab.smtp.transport.connection.ClosableSmtpConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProjectController implements Runnable {
    private ProjectConf project;
    private SqlConnectionPool sqlConnectionPool;
    private Connection connection;
    private List<Statistic> statisticList = new ArrayList<>();

    public ProjectController(ProjectConf project) {
        this.project = project;
    }


    private void sendMail() {
        new SendOverMailConfig(project).sendMail();
    }

    private void configureSqlPool() {
        if (StringUtils.isBlank(project.getDataBaseUser()) || StringUtils.isBlank(project.getDataBasePassword())) {
            sqlConnectionPool = new SqlConnectionPool(project.getConnectURL());
        } else {
            sqlConnectionPool = new SqlConnectionPool(project.getConnectURL(), project.getDataBaseUser(), project.getDataBasePassword());
        }
    }

    private void getR4000Data() {
        System.out.println("получение данных по R4000");
        connection = sqlConnectionPool.getConnection();
        try {
            final Statement statement = connection.createStatement();
            final String date = Config.getInstance().getDate();
            String SQL =
                            "select MAX(ChangeDate) ChangeDate, idRecord\n" +
                            "into #cd1\n" +
                            "from v_LogDataChange \n" +
                            "where tableName = 'refDistributorsExt' \n" +
                            "and idRecord in (select id from refDistributors) and FieldName = 'usereplicator4000'\n" +
                            "group by idRecord;\n" +


                            "select eal.TenantId, MIN(LastSync) firstSync, MAX(LastSync) lastSync\n" +
                            "into #statistc\n" +
                            "from hub.ExchangeAuditLog eal\n" +
                            "where Date >=\'" + date + "\' \n" +
                            "group by TenantId;\n" +


                            "with st as(\n" +
                            "select ChangeDate, idRecord, NewValue useReplicator4000Log from v_LogDataChange where (ChangeDate in (select ChangeDate from #cd1) and idRecord in (select idRecord from #cd1))\n" +
                            "),\n" +
                            "finalStat as \n" +
                            "(select rde.UseReplicator4000 useReplicator4000, rde.id idDistr,st.ChangeDate ChangeDate, st.idRecord, st.useReplicator4000Log useReplicator4000Log from refDistributorsExt  rde\n" +
                            "left join st on st.idRecord = rde.id\n" +
                            ")\n" +
                            "select rd.NodeID, rd.id,  rd.Name NameOfDistributors, firstSync FirstSession, lastSync LastSession, fs.ChangeDate dateOfChange, fs.useReplicator4000, fs.useReplicator4000Log from #statistc st\n" +
                            "join refDistributors rd on rd.NodeID = st.TenantId\n" +
                            "join finalStat fs on fs.idDistr = rd.id \n" +
                            "drop table #cd1,#statistc";

            final ResultSet resultSet = statement.executeQuery(SQL);

            while (resultSet.next()) {
                String realValue = resultSet.getString("useReplicator4000");
                String dateOfChange = resultSet.getString("dateOfChange");
                String status;
                if (!realValue.equals(resultSet.getString("useReplicator4000Log"))) {
                    dateOfChange = null;
                }
                switch (realValue) {
                    case "":
                    case "null":
                    case " ":
                    case "0":
                        status = "Disabled";
                        break;
                    default:
                        status = "Enabled";
                        break;
                }
                final Statistic tmpStatistic = new Statistic(resultSet.getString("NameOfDistributors"),
                        resultSet.getString("NodeID"),
                        resultSet.getString("id"),
                        dateOfChange,
                        resultSet.getString("FirstSession"),
                        resultSet.getString("LastSession"),
                        status,
                        Protocol.R4000
                );
                statisticList.add(tmpStatistic);
            }
            //  statisticList.forEach(System.out::println);
            if (statisticList.size() == 0) {
                System.out.println("R4000 не использовался в этом периоде");
                System.exit(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (LackOfInformationException e) {
            e.printStackTrace();
        } finally {
            sqlConnectionPool.returnConnection(connection);
        }
    }

    private void getCiceroneData() {
        System.out.println("получение данных по Cicerone");
        connection = sqlConnectionPool.getConnection();
        try {
            final Statement statement = connection.createStatement();
            final String date = Config.getInstance().getDate();
            String SQL =
                    "select st.DistributorId statistcDistr, cd.id enabledDistr, rd.name enabledName, rd.NodeID enabledNodeID, rd2.name statisticName, rd2.NodeID statisticNodeID, firstSync,lastSync,Protocol from cicerone.Distributors cd \n" +
                            "full outer join ( \n" +
                            "select DistributorId, MIN(SessionCreateDate) firstSync, MAX(SessionCreateDate) lastSync \n" +
                            "from cicerone.Sessions \n" +
                            "where SessionCreateDate >='20190101'\n" +
                            "group by DistributorId)  st on st.DistributorId = cd.id \n" +
                            "left join refDistributors rd on cd.id = rd.id \n" +
                            "left join refDistributors rd2 on st.DistributorId = rd2.id;";
            final ResultSet resultSet = statement.executeQuery(SQL);
            List<Statistic> tmpListCicerone = new ArrayList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (resultSet.next()) {
                String name = resultSet.getString("enabledName");
                String nodeId = resultSet.getString("enabledNodeID");
                String distrId = resultSet.getString("enabledDistr");

                if (name == null) name = resultSet.getString("statisticName");
                if (nodeId == null) nodeId = resultSet.getString("statisticNodeID");
                if (distrId == null) distrId = resultSet.getString("statistcDistr");

                String protocolStatus = resultSet.getString("Protocol");

                String tmpStatus;

                if (StringUtils.isBlank(protocolStatus)) {
                    tmpStatus = "Disabled";
                } else tmpStatus = "Enabled";
                Date firstSyncD = null;
                Date lastSyncD = null;
                String firstSync = resultSet.getString("firstSync");
                if (firstSync != null) {
                    firstSyncD = sdf.parse(resultSet.getString("firstSync"));
                    lastSyncD = sdf.parse(resultSet.getString("lastSync"));
                }
                final Statistic tmpStatistic = new Statistic(
                        name,
                        distrId,
                        nodeId,
                        firstSyncD,
                        lastSyncD,
                        tmpStatus,
                        Protocol.Cicerone
                );
                tmpListCicerone.add(tmpStatistic);
            }
            combineR4000AndCiceroneData(tmpListCicerone);
            statisticList = deleteMarkedElements(statisticList);
        } catch (SQLException | LackOfInformationException | ParseException e) {
            if (e.getMessage().contains("Invalid object name 'cicerone.Sessions'")) {
                System.out.println("Данные по протоколу Cicerone отсутствуют");
            } else

                e.printStackTrace();
        } finally {
            sqlConnectionPool.returnConnection(connection);
        }
    }

    private void combineR4000AndCiceroneData(List<Statistic> tmpListCicerone) {
        System.out.println("CombiningR4000AndCiceroneData");
        for (Statistic tmpStatistic : tmpListCicerone) {
            for (Statistic statistic : statisticList) {
                if (statistic.getDistrId().equals(tmpStatistic.getDistrId())) {
                    if (tmpStatistic.getStatus().equals("Enabled") && statistic.getStatus().equals("Disabled")) {
                        tmpStatistic.setDateOfChange(statistic.getDateOfChange());
                        statistic.setDeletedMark(true);
                    }
                    if (tmpStatistic.getStatus().equals("Disabled") && statistic.getStatus().equals("Enabled")) {
                        tmpStatistic.setDeletedMark(true);
                    }
                    if (tmpStatistic.getStatus().equals("Disabled") && statistic.getStatus().equals("Disabled")) {
                        if (tmpStatistic.getFirstSession().after(statistic.getFirstSession())) {
                            tmpStatistic.setDateOfChange(statistic.getDateOfChange());
                            statistic.setDeletedMark(true);
                        } else {
                            tmpStatistic.setDeletedMark(true);
                        }
                    }
                }
            }
        }
        statisticList.addAll(tmpListCicerone);
        //  System.out.println(statisticList);
    }

    private List<Statistic> deleteMarkedElements(List<Statistic> tmpList) {
        System.out.println("удаление дубликатов");
        return tmpList.stream()
                .filter(e -> !e.isDeletedMark())
                .collect(Collectors.toList());
    }

    private void createCSV() {
        final CSVCreator csvCreator = new CSVCreator(project.getDataBase());
        csvCreator.convertToCSV(statisticList);
    }

    private void createExcel() {
        final ExcelCreator excelCreator = new ExcelCreator(project.getDataBase());
        excelCreator.converToExcel(statisticList);
    }

    private void createHTML() {
        HTMLCreator htmlCreator = new HTMLCreator(project.getDataBase());
        htmlCreator.convertToHMTL(statisticList);
    }

    private void clearRecentExportAndCreate() {
        System.out.println(project.getDataBase());
        String tmpPath = CurrentPath.getInstance().getPath();
        Path pathCSV = Paths.get(tmpPath + "\\" + project.getDataBase() + "\\" + "statistic.csv");
        Path pathExcel = Paths.get(tmpPath + "\\" + project.getDataBase() + "\\" + "statistic.html");
        Path pathHTML = Paths.get(tmpPath + "\\" + project.getDataBase() + "\\" + "statistic.xls");
        Path mainPath = Paths.get(tmpPath + "\\" + project.getDataBase());

        try {
            if (Files.exists(mainPath)) {
                if (Files.exists(pathCSV)) {
                    Files.delete(pathCSV);
                }
                if (Files.exists(pathExcel)) {
                    Files.delete(pathExcel);
                }
                if (Files.exists(pathHTML)) {
                    Files.delete(pathHTML);
                }
            } else {
                Files.createDirectory(mainPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        clearRecentExportAndCreate();
        configureSqlPool();
        getR4000Data();
        getCiceroneData();
        createCSV();
        createExcel();
        createHTML();
        sendMail();
    }
}
