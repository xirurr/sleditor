package Base;

import Services.CurrentPath;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

@Data
public class Config {
    private String date;

    private List<ProjectConf> projectConfList = new ArrayList<>();
    private DocumentBuilderFactory dbf = null;
    private DocumentBuilder db = null;
    private Document doc = null;
    private Character delimiter;
    private List<String> supervisorRecipients = new ArrayList<>();
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
                readProjectsConf(eElement);
                date = eElement.getElementsByTagName("date").item(0).getTextContent();
                delimiter = eElement.getElementsByTagName("delimiter").item(0).getTextContent().charAt(0);
                supervisorRecipients = Arrays.asList(eElement.getElementsByTagName("supervisorRecipients").item(0).getTextContent().split(";"));
                mailServer = eElement.getElementsByTagName("mailServer").item(0).getTextContent();
                mailPort = eElement.getElementsByTagName("mailPort").item(0).getTextContent();
                mailUser = eElement.getElementsByTagName("mailUser").item(0).getTextContent();
                mailPassword = eElement.getElementsByTagName("mailPassword").item(0).getTextContent();
            }
        }
    }

    private void readConfig() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("Reading config.xml file");
        dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();
        String configPath = CurrentPath.getInstance().getPath();
        doc = db.parse(configPath + "/config.xml");
        doc.getDocumentElement().normalize();
    }

    private void readProjectsConf(Element eElement) {
        NodeList custom = eElement.getElementsByTagName("custom");
        for (int itr = 0; itr < custom.getLength(); itr++) {
            Node node = custom.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element innerElement = (Element) node;
                NodeList project = innerElement.getElementsByTagName("project");
                for (int i = 0; i < project.getLength(); i++) {
                    Node projectNode = project.item(i);
                    if (projectNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element projectNodeElement = (Element) projectNode;
                        String server = projectNodeElement.getElementsByTagName("server").item(0).getTextContent();
                        String dataBase = projectNodeElement.getElementsByTagName("dataBase").item(0).getTextContent();
                        String dataBaseUser = projectNodeElement.getElementsByTagName("dataBaseUser").item(0).getTextContent();
                        String dataBasePassword = projectNodeElement.getElementsByTagName("dataBasePassword").item(0).getTextContent();
                        ArrayList<String> recipients = new ArrayList<>(Arrays.
                                asList(projectNodeElement.getElementsByTagName("recipients").item(0).getTextContent().split(";")));
                        if (recipients.size() == 1 && recipients.get(0).equals("")) {
                            recipients.remove(0);
                        }
                        String port = projectNodeElement.getElementsByTagName("port").item(0).getTextContent();
                        if (port.toLowerCase().equals("default") || StringUtils.isBlank(port)) {
                            port = ";";
                        } else {
                            port = ":" + port + ";";
                        }
                        ProjectConf projectConf = new ProjectConf(server, port, dataBase, dataBaseUser, dataBasePassword, recipients);
                        projectConfList.add(projectConf);
                    }
                }
            }
        }
    }
}
