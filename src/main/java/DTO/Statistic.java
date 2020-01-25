package DTO;

import Exceptions.LackOfInformationException;
import SQL.SqlConnectionPool;
import lombok.Data;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;


@Data
public class Statistic {
    private String nameOfDistr = null;
    private Long nodeId;
    private Long distrId;
    private Date dateOfChange = null;
    private Date lastSession = null;
    private Date firstSession = null;
    private String status;
    private boolean deletedMark = false;
    private Protocol protocol;



    public Statistic(String nameOfDistr, String nodeId, String distrId,
                     String dateOfChange, String firstSession, String lastSession, String useReplicatorOldValue,
                     String useReplicatorNewValue, Protocol r4000) throws LackOfInformationException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

        if (nameOfDistr == null || nameOfDistr.isBlank()) {
            this.nameOfDistr = "empty";
        } else {
            this.nameOfDistr = nameOfDistr;
        }

        if (nodeId == null || nodeId.isBlank()) {
            throw new LackOfInformationException("nodeId cannot be empty");
        } else {
            this.nodeId = Long.parseLong(nodeId);
        }

        if (distrId == null || distrId.isBlank()) {
            throw new LackOfInformationException("distrId cannot be empty");
        } else {
            this.distrId = Long.parseLong(distrId);
        }
        try {
            if (dateOfChange == null || dateOfChange.isBlank()) {
                this.dateOfChange = null;
            } else {
                this.dateOfChange = sdf.parse(dateOfChange);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!firstSession.isBlank()) {
            try {
                sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSSS");
                this.firstSession = sdf.parse(firstSession);
                this.lastSession = sdf.parse(lastSession);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        status = checkDistrStatus(useReplicatorOldValue, useReplicatorNewValue);
        this.protocol = r4000;
    }


    public Statistic(String nameOfDistr, String nodeId, String distributorFromSessions,
                     String sessionDate, String connectedDistributor,
                     Protocol cicerone) throws LackOfInformationException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        if (nameOfDistr == null || nameOfDistr.isEmpty()) {
            this.nameOfDistr = "empty";
        } else {
            this.nameOfDistr = nameOfDistr;
        }

        if (nodeId == null || nodeId.isBlank()) {
            throw new LackOfInformationException("nodeId cannot be empty");
        } else {
            this.nodeId = Long.parseLong(nodeId);
        }

        if (distributorFromSessions == null || distributorFromSessions.isBlank()) {
            throw new LackOfInformationException("distributorFromSessions cannot be empty");
        } else {
            this.distrId = Long.parseLong(distributorFromSessions);
        }
        try {
            if (sessionDate == null || sessionDate.isEmpty()) {
                throw new LackOfInformationException("Cicerone session date cannot be empty");
            } else {
                this.firstSession = sdf.parse(sessionDate);
                this.lastSession = sdf.parse(sessionDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        status = connectedDistributor.isBlank() ? "Disabled" : "Enabled";
        this.protocol = cicerone;
    }

    public Statistic(String name, String nodeId, String firstSync, String lastSync, Protocol protocol) throws LackOfInformationException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        if (name == null || name.isBlank()) {
            this.nameOfDistr = "empty";
        } else {
            this.nameOfDistr = name;
        }

        if (nodeId == null || nodeId.isBlank()) {
            throw new LackOfInformationException("nodeId cannot be empty");
        } else {
            this.nodeId = Long.parseLong(nodeId);
        }
        try {
            if (firstSync.isBlank()) {
                throw new LackOfInformationException("Cicerone session date cannot be empty");
            } else {
                this.firstSession = sdf.parse(firstSync);
                this.lastSession = sdf.parse(lastSync);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.protocol = protocol;
        status

    }


    private String checkDistrStatus(String useReplicatorOldValue, String useReplicatorNewValue) {
        Boolean oldBoolValue;
        Boolean newBoolValue;
        if (useReplicatorOldValue == null || useReplicatorOldValue.isEmpty()) {
            oldBoolValue = null;
        } else {
            oldBoolValue = Integer.parseInt(useReplicatorOldValue) != 0;
        }
        if (useReplicatorNewValue == null || useReplicatorNewValue.isEmpty()) {
            newBoolValue = null;
        } else {
            newBoolValue = Integer.parseInt(useReplicatorNewValue) != 0;
        }
        if (oldBoolValue == null || newBoolValue == null) {
            return getCurrentStatusFromDb();
        } else if (oldBoolValue == false && newBoolValue == false) {
            return "Disabled";
        } else if (oldBoolValue == false && newBoolValue == true) {
            return "Enabled";
        } else if (oldBoolValue == true && newBoolValue == false) {
            return "Disabled";
        } else {
            return "Undefined";
        }


    }

    public Map<String, String> listElements() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put(nameOfDistr, "Имя дистрибьютора");
        parameters.put(nodeId.toString(), "NodeId");
        parameters.put(distrId.toString(), "ID дистрибьютора");
        parameters.put(
                dateOfChange == null ? "  " : sdf.format(dateOfChange), "Дата включения/отключения R4000");
        parameters.put(protocol.toString(), "протокол");
        parameters.put(status, "состояние");
        parameters.put(
                firstSession == null ? "" : sdf.format(firstSession), "первая сессия периода");
        parameters.put(
                lastSession == null ? " " : sdf.format(lastSession), "последняя сессия");
        return parameters;
    }


    public String getCurrentStatusFromDb() {
        final SqlConnectionPool sqlConnectionPool = new SqlConnectionPool();
        final Connection connection = sqlConnectionPool.getConnection();
        try {
            final Statement statement = connection.createStatement();
            String SQL =
                    "Select UseReplicator4000 from refDistributorsExt  where id =" + distrId;
            final ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                if (resultSet.getString("UseReplicator4000").equals("0")) {
                    return "Disabled";
                }
                if (!resultSet.getString("UseReplicator4000").isEmpty()) {
                    return "Enabled";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqlConnectionPool.returnConnection(connection);
        }
        return "Undefined";
    }


}
