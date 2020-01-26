package Base;

import Exceptions.EmptyConfigFieldException;
import Services.CurrentPath;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Data
public class Config {
    private String date;
    private String dataBase;
    private String server;
    private String port;
    private String dataBaseUser = "";
    private String dataBasePassword = "";
    private String connectURL;
    private DocumentBuilderFactory dbf = null;
    private DocumentBuilder db = null;
    private Document doc = null;
    private Character delimiter;
    private List<String> recipients = new ArrayList<>();
    private String mailServer;
    private String mailPort;
    private String mailUser;
    private String mailPassword;


    private static Config instance;

    private Config() {
        try {
            readConfig();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Проблемы с чтением файла config.xml");
            System.exit(1);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        loadConfig();
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }


    private void loadConfig() {
        System.out.println("load configuration");
        final NodeList config = doc.getElementsByTagName("config");
        for (int itr = 0; itr < config.getLength(); itr++) {
            Node node = config.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                date = eElement.getElementsByTagName("date").item(0).getTextContent();
                server = eElement.getElementsByTagName("server").item(0).getTextContent();
                dataBase = eElement.getElementsByTagName("dataBase").item(0).getTextContent();
                dataBaseUser = eElement.getElementsByTagName("dataBaseUser").item(0).getTextContent();
                dataBasePassword = eElement.getElementsByTagName("dataBasePassword").item(0).getTextContent();
                port = eElement.getElementsByTagName("port").item(0).getTextContent();
                if (port.toLowerCase().equals("default") || port.isBlank()) {
                    port = ";";
                } else {
                    port = ":" + port + ";";
                }
                delimiter = eElement.getElementsByTagName("delimiter").item(0).getTextContent().charAt(0);
                recipients = Arrays.asList(eElement.getElementsByTagName("recipients").item(0).getTextContent().split(";"));

                mailServer = eElement.getElementsByTagName("mailServer").item(0).getTextContent();
                mailPort = eElement.getElementsByTagName("mailPort").item(0).getTextContent();
                mailUser = eElement.getElementsByTagName("mailUser").item(0).getTextContent();
                mailPassword = eElement.getElementsByTagName("mailPassword").item(0).getTextContent();
            }
        }
        createConnectionURL();
    }

    private void readConfig() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("Reading config.xml file");
        dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();
        String configPath = CurrentPath.getInstance().getPath();
        doc = db.parse(configPath+"/config.xml");
        doc.getDocumentElement().normalize();
    }

    private void createConnectionURL() {
        //  connectURL = "jdbc:sqlserver://" + server + "\\MSSQLSERVER;database=" + dataBase;
        //connectURL = "jdbc:sqlserver://" + server+port+"database=" + dataBase;
           // connectURL = "jdbc:sqlserver://" + server + "\\SPLAT;database=" + dataBase;
           connectURL = "jdbc:sqlserver://" + server+port+"database=" + dataBase;

    }

    public Map<String, Object> getMapMailConfig() {
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("mailServer", mailServer);
        objectObjectHashMap.put("mailPort", mailPort);
        objectObjectHashMap.put("mailUser", mailUser);
        objectObjectHashMap.put("mailPassword", mailPassword);
        objectObjectHashMap.put("recipients", recipients.toString());
        return objectObjectHashMap;
    }


}
