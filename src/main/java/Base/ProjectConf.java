package Base;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class ProjectConf {
    private String server;
    private String port;
    private String dataBase;
    private String dataBaseUser = "";
    private String dataBasePassword = "";
    private String connectURL;
    private List<String> recipientsForOne = new ArrayList<>();
    private Boolean deleted = false;

    public ProjectConf(String server, String port, String dataBase, String dataBaseUser, String dataBasePassword, List<String> recipientsForOne) {
        this.server = server;
        this.port = port;
        this.dataBase = dataBase;
        this.dataBaseUser = dataBaseUser;
        this.dataBasePassword = dataBasePassword;
        this.recipientsForOne = recipientsForOne;
        createConnectionURL();
    }

    private void createConnectionURL() {
        connectURL = "jdbc:sqlserver://" + server + port + "database=" + dataBase;

    }

}
