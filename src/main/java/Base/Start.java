package Base;

import DTO.Protocol;
import DTO.Statistic;
import Exceptions.LackOfInformationException;
import FileCreators.CSVCreator;
import FileCreators.ExcelCreator;
import FileCreators.HTMLCreator;
import Mail.MailService;
import Mail.SendOverMailConfig;
import SQL.SqlConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Start {
    private Config config;
    private String connectURL;
    private SqlConnectionPool sqlConnectionPool;
    private Connection connection;
    private List<Statistic> statisticList = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(10);
        final Start start = new Start();
        start.configure();
        start.getR4000Data();
        start.getCiceroneData();
        start.createCSV();
        start.createExcel();
        start.createHTML();
        start.sendMail();
    }

    private void sendMail() {
/*        final SendOverSql sendOverSql = new SendOverSql();
        sendOverSql.sendMail();*/
        MailService sendOverMailConfig = new SendOverMailConfig();
        sendOverMailConfig.sendMail();
    }


    private void configure() {
        config = Config.getInstance();
        connectURL = config.getConnectURL();
        if (config.getDataBaseUser().isBlank() || config.getMailPassword().isBlank()) {
            sqlConnectionPool = new SqlConnectionPool(connectURL);
        } else {
            sqlConnectionPool = new SqlConnectionPool(connectURL, config.getDataBaseUser(), config.getDataBasePassword());
        }

    }


    private void getR4000Data() {
        System.out.println("получение данных по R4000");
        connection = sqlConnectionPool.getConnection();
        try {
            final Statement statement = connection.createStatement();
            final String date = config.getDate();

            String SQL =
                    " with statistc as (\n" +
                            "select eal.TenantId, MIN(LastSync) firstSync, MAX(LastSync) lastSync\n" +
                            "from hub.ExchangeAuditLog eal\n" +
                            "where Date >=\'" + date + "\' \n" +
                            "group by TenantId),\n" +
                            "cd as (\n" +
                            "select MAX(ChangeDate) ChangeDate, idRecord from v_LogDataChange where tableName = 'refDistributorsExt' \n" +
                            "and idRecord in (select id from refDistributors) and FieldName = 'usereplicator4000'\n" +
                            "group by idRecord\n" +
                            "),\n" +
                            "st as(\n" +
                            "select ChangeDate, idRecord, NewValue useReplicator4000Log from v_LogDataChange where (ChangeDate in (select ChangeDate from cd) and idRecord in (select idRecord from cd))\n" +
                            "),\n" +
                            "finalStat as (select rde.UseReplicator4000 useReplicator4000, rde.id idDistr,st.ChangeDate ChangeDate, st.idRecord, st.useReplicator4000Log useReplicator4000Log from refDistributorsExt  rde\n" +
                            "left join st on st.idRecord = rde.id\n" +
                            ")\n" +
                            "select rd.NodeID, rd.id,  rd.Name NameOfDistributors, firstSync FirstSession, lastSync LastSession, fs.ChangeDate dateOfChange, fs.useReplicator4000, fs.useReplicator4000Log from statistc st\n" +
                            "join refDistributors rd on rd.NodeID = st.TenantId\n" +
                            "join finalStat fs on fs.idDistr = rd.id \n";

            final ResultSet resultSet = statement.executeQuery(SQL);

            List<Statistic> tmpListCicerone = new ArrayList<>();
            int count = 0;
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
                count++;
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
            final String date = config.getDate();
            String SQL =
                    "with stat as(\n" +
                            "select DistributorId, MIN(SessionCreateDate) firstSync, MAX(SessionCreateDate) lastSync\n" +
                            "from cicerone.Sessions\n" +
                            "where SessionCreateDate >=\'" + date + "\' \n" +
                            "group by DistributorId)\n" +
                            "select DistributorId, rd.name Name, rd.NodeID, firstSync, lastSync, Protocol from stat \n" +
                            "left join cicerone.Distributors cd on cd.id = stat.DistributorId\n" +
                            "join refDistributors rd on stat.DistributorId = rd.id";

            final ResultSet resultSet = statement.executeQuery(SQL);
            List<Statistic> tmpListCicerone = new ArrayList<>();
            int count = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            while (resultSet.next()) {
                String protocolStatus = resultSet.getString("Protocol");
                String tmpStatus;
                if (protocolStatus.isBlank() || protocolStatus.toLowerCase().equals("null")) {
                    tmpStatus = "Disabled";
                } else tmpStatus = "Enabled";
                final Statistic tmpStatistic = new Statistic(
                        resultSet.getString("Name"),
                        resultSet.getString("DistributorId"),
                        resultSet.getString("NodeID"),
                        sdf.parse(resultSet.getString("firstSync")),
                        sdf.parse(resultSet.getString("lastSync")),
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
        System.out.println(tmpListCicerone.size());
        System.out.println(statisticList.size());

        statisticList.addAll(tmpListCicerone);
    }


    private List<Statistic> deleteMarkedElements(List<Statistic> tmpList) {
        System.out.println("удаление дубликатов");
        return tmpList.stream()
                .filter(e -> !e.isDeletedMark())
                .collect(Collectors.toList());
    }


    private void createCSV() {
        final CSVCreator csvCreator = new CSVCreator();
        csvCreator.convertToCSV(statisticList);
    }

    private void createExcel() {
        final ExcelCreator excelCreator = new ExcelCreator();
        excelCreator.converToExcel(statisticList);
    }

    private void createHTML() {
        HTMLCreator htmlCreator = new HTMLCreator();
        htmlCreator.convertToHMTL(statisticList);
    }

    private int getSize(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.last();
            return rs.getRow();
        }
        return 0;
    }
}
