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
                     String dateOfChange, String fSessionTime, String lSessionTime,String status, Protocol r4000) throws LackOfInformationException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        if (!fSessionTime.isBlank()) {
            try {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.firstSession = sdf.parse(fSessionTime);
                this.lastSession = sdf.parse(lSessionTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.status = status;
        this.protocol = r4000;
    }




    public Statistic(String name, String distrId, String nodeId, Date firstSync, Date lastSync,String status, Protocol cicerone) throws LackOfInformationException {

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
        this.protocol = cicerone;
        this.status = status;

    }


    public Map<String, String> listElements() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("Имя дистрибьютора",nameOfDistr);
        parameters.put( "NodeId",nodeId.toString());
        parameters.put("ID дистрибьютора",distrId.toString());
        parameters.put(
                 "Дата включения/отключения R4000",dateOfChange == null ? "  " : sdf.format(dateOfChange));
        parameters.put("протокол",protocol.toString());
        parameters.put( "состояние",status);
        parameters.put(
                "первая сессия периода", firstSession == null ? "не было сессий в периоде" : sdf.format(firstSession));
        parameters.put(
               "последняя сессия периода", lastSession == null ? "" : sdf.format(lastSession));
        return parameters;
    }

}
