package DTO;

import Exceptions.LackOfInformationException;
import SQL.SqlConnectionPool;
import lombok.Data;

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
                     String dateOfChange, String firstSession, String lastSession,String status, Protocol r4000) throws LackOfInformationException {

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
        this.status = status;
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


    public Statistic(String name, String distrId, String nodeId, Date firstSync, Date lastSync,String status, Protocol protocol) throws LackOfInformationException {

        if (name == null || name.isBlank()) {
            this.nameOfDistr = "empty";
        } else {
            this.nameOfDistr = name;
        }
        if (distrId == null || distrId.isBlank()) {
            throw new LackOfInformationException("distrId cannot be empty");
        } else {
            this.distrId = Long.parseLong(distrId);
        }
        if (nodeId == null || nodeId.isBlank()) {
            throw new LackOfInformationException("nodeId cannot be empty");
        } else {
            this.nodeId = Long.parseLong(nodeId);
        }
        this.firstSession = firstSync;
        this.lastSession = lastSync;
        this.protocol = protocol;
        this.status = status;

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

}
