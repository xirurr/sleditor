package FileCreators;

import Services.CurrentPath;
import lombok.Data;

@Data
public abstract class AbstractCreator {

    private String finalPath;
    public AbstractCreator() {
    }


    public AbstractCreator(String dbName) {
        String path = CurrentPath.getInstance().getPath();
        this.finalPath = path + "\\" + dbName + "\\";
    }
}
