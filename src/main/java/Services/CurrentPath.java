package Services;

import Base.Config;
import lombok.Data;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Data
public class CurrentPath {
    private String path;
    private static CurrentPath instance;


    private CurrentPath() {
        writePath();
    }

    public static CurrentPath getInstance() {
        if (instance == null) {
            instance = new CurrentPath();
        }
        return instance;
    }

    private void writePath() {
        File jarPath = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        path = jarPath.getParentFile().getAbsolutePath()+"\\";
    }
}
