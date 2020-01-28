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

}
