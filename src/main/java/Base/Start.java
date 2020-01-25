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

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setOut;

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

    /*private void getR4000DataOld() {
        connection = sqlConnectionPool.getConnection();
        try {
            final Statement statement = connection.createStatement();
            final String date = config.getDate();
            String SQL =
                    "with mt as\n" +
                            "(\n" +
                            "select  TenantId from hub.ExchangeAuditLog with (nolock)\n" +
                            "where Date >=\'" + date + "\' \n" +
                            "group by TenantId\n" +
                            "),\n" +
                            "logInfo as\n" +
                            "(\n" +
                            "select * from v_LogDataChange where TableName ='refDistributorsExt' and FieldName = 'usereplicator4000'\n" +
                            ")\n" +
                            "\n" +
                            "select  d.Name as NameOfDistributors, d.NodeID,d.id, li.ChangeDate dateOfChange,li.OldValue useReplicatorOldValue, li.NewValue useReplicatorNewValue\n" +
                            "from mt\n" +
                            "join dbo.refDistributors as d on mt.TenantId = d.NodeID\n" +
                            "left join logInfo as li on li.idRecord = d.id\n";
            final ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                final Statistic tmpStatistic = new Statistic(resultSet.getString("NameOfDistributors"),
                        resultSet.getString("NodeID"),
                        resultSet.getString("id"),
                        resultSet.getString("dateOfChange"),
                        resultSet.getString("useReplicatorOldValue"),
                        resultSet.getString("useReplicatorNewValue"),
                        Protocol.R4000
                );
                checkAndMarkDuplicates(tmpStatistic);
                statisticList.add(tmpStatistic);
            }
            deleteMarkedElements();
            statisticList.forEach(System.out::println);

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
    }*/

    private void getR4000Data() {
        System.out.println("получение данных по R4000");
        connection = sqlConnectionPool.getConnection();
        try {
            final Statement statement = connection.createStatement();
            final String date = config.getDate();

            String SQL =
                    " with mt as \n" +
                            "                            ( \n" +
                            "select  TenantId,MAX(LastSync) LastSync,MIN(LAstSync) FirstSync from hub.ExchangeAuditLog with (nolock) \n" +
                            "where Date >=\'" + date + "\' \n" +
                            "group by TenantId\n" +
                            "), \n" +
                            "logInfo as \n" +
                            "( \n" +
                            "select * from v_LogDataChange with (nolock) where TableName ='refDistributorsExt' and FieldName = 'usereplicator4000'\n" +
                            ") \n" +
                            " \n" +
                            "select  d.Name as NameOfDistributors, d.NodeID,d.id,li.ChangeDate dateOfChange,\n" +
                            "li.OldValue useReplicatorOldValue,li.NewValue useReplicatorNewValue ,mt.LastSync LastSession, mt.FirstSync FirstSession\n" +
                            "from mt \n" +
                            "join dbo.refDistributors as d on mt.TenantId = d.NodeID\n" +
                            "left join logInfo as li on li.idRecord = d.id";

            final ResultSet resultSet = statement.executeQuery(SQL);

            List<Statistic> tmpListCicerone = new ArrayList<>();
            int count = 0;
            while (resultSet.next()) {
                final Statistic tmpStatistic = new Statistic(resultSet.getString("NameOfDistributors"),
                        resultSet.getString("NodeID"),
                        resultSet.getString("id"),
                        resultSet.getString("dateOfChange"),
                        resultSet.getString("FirstSession"),
                        resultSet.getString("LastSession"),
                        resultSet.getString("useReplicatorOldValue"),
                        resultSet.getString("useReplicatorNewValue"),
                        Protocol.R4000
                );
                count++;

                tmpListCicerone = compareSessions(tmpListCicerone, tmpStatistic);
                //  checkAndMarkDuplicates(tmpStatistic);

            }
            System.out.println("обработано " + count + " сессий R4000");
            deleteMarkedElements();
            statisticList.addAll(tmpListCicerone);
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
                    "with statistc as (\n" +
                            "select DistributorId, MIN(SessionCreateDate) firstSync, MAX(SessionCreateDate) lastSync\n" +
                            "from cicerone.Sessions  \n" +
                            "where SessionCreateDate >=\'" + date + "\' \n" +
                            "group by DistributorId)\n" +
                            "select rd.NodeID, rd.id distrId rd.Name, firstSync, lastSync from statistc st\n" +
                            "join refDistributors rd on rd.id = st.DistributorId";

            final ResultSet resultSet = statement.executeQuery(SQL);
            List<Statistic> tmpListCicerone = new ArrayList<>();
            int count = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            while (resultSet.next()) {
                final Statistic tmpStatistic = new Statistic(
                        resultSet.getString("Name"),
                        resultSet.getString("distrId"),
                        resultSet.getString("NodeID"),
                        sdf.parse(resultSet.getString("firstSync")),
                        sdf.parse(resultSet.getString("lastSync")),
                        Protocol.Cicerone
                );

                tmpListCicerone = compareSessions(tmpListCicerone, tmpStatistic);
            }

            combineR4000AndCiceroneData(tmpListCicerone);
            System.out.println("обработано " + count + " сессий Cicerone");
            deleteMarkedElements();
        } catch (SQLException | LackOfInformationException | ParseException e) {
            if (e.getMessage().contains("Invalid object name 'cicerone.Sessions'")) {
                System.out.println("Данные по протоколу Cicerone отсутствуют");
            } else
                e.printStackTrace();
        } finally {
            sqlConnectionPool.returnConnection(connection);
        }
    }

    private void combineR4000AndCiceroneData(List<Statistic> tmpListCicerone) { //проверять какой протокол и в зависимости от этого выводить последнюю статистику
        System.out.println("CombiningR4000AndCiceroneData");
        for (Statistic tmpStatistic : tmpListCicerone) {
            for (Statistic statistic : statisticList) {
                if (statistic.getDistrId().equals(tmpStatistic.getDistrId())) {
                    statistic.setProtocol(Protocol.R4000andCicerore);
                    statistic.setStatus("Cicerone: " + tmpStatistic.getStatus() + " " + "R4000: " + statistic.getStatus());
                    if (tmpStatistic.getStatus().equals("Enabled") && statistic.getStatus().equals("Disabled")) {
                        statistic.setFirstSession(tmpStatistic.getFirstSession());
                        statistic.setLastSession(tmpStatistic.getLastSession());
                    }
                    tmpStatistic.setDeletedMark(true);

                }
            }
        }
        System.out.println(tmpListCicerone.size());
        System.out.println(statisticList.size());
        statisticList.addAll(tmpListCicerone);
    }

    private List<Statistic> compareSessions(List<Statistic> tmpStatisticList, Statistic tmpStatistic) {
        if (tmpStatisticList.size() == 0) {
            tmpStatisticList.add(tmpStatistic);
            return tmpStatisticList;
        }
        Statistic objectToRemove = null;
        for (Statistic statistic : tmpStatisticList) {
            if (statistic.getDistrId().equals(tmpStatistic.getDistrId())) {
                tmpStatistic.setFirstSession(statistic.getFirstSession().before(tmpStatistic.getFirstSession()) ? statistic.getFirstSession() : tmpStatistic.getFirstSession());
                tmpStatistic.setLastSession(statistic.getLastSession().after(tmpStatistic.getLastSession()) ? statistic.getLastSession() : tmpStatistic.getLastSession());
                if (statistic.getDateOfChange() != null)
                    tmpStatistic.setDateOfChange(statistic.getDateOfChange().after(tmpStatistic.getDateOfChange()) ? statistic.getDateOfChange() : tmpStatistic.getDateOfChange());
                objectToRemove = statistic;
            }
        }
        tmpStatisticList.remove(objectToRemove);
        tmpStatisticList.add(tmpStatistic);
        return tmpStatisticList;
    }

    private void checkAndMarkDuplicates(Statistic tmp) {
        for (Statistic statistic : statisticList) {
            if (statistic.getDistrId().equals(tmp.getDistrId())) {
                if (statistic.getDateOfChange().before(tmp.getDateOfChange())) {
                    statistic.setDeletedMark(true);
                } else {
                    tmp.setDeletedMark(true);
                }
            }
        }
    }

    private void deleteMarkedElements() {
        System.out.println("удаление дубликатов");
        statisticList = statisticList.stream()
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
