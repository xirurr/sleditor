package Base;

import DTO.Protocol;
import DTO.Statistic;
import Exceptions.LackOfInformationException;
import FileCreators.CSVCreator;
import FileCreators.ExcelCreator;
import FileCreators.HTMLCreator;
import Mail.MailService;
import Mail.SendOverMailConfig;
import Pools.SqlConnectionPool;
import Pools.ThreadPool;
import Services.CurrentPath;
import Services.PathHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Start {
    private Config config;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    static {
        try {
            String configPath = CurrentPath.getInstance().getPath();
            PathHelper.appendToPath(configPath);

        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        System.out.println(10);
        final Start start = new Start();
        start.startIt();

    }
    private void startIt(){
        config();
        clearDuplicateProjects();
        workWithProjects();

        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
                threadPool.shutdownNow();
            System.out.println("пул выключен");
            sendMail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMail() {
        MailService sendOverMailConfig = new SendOverMailConfig();
        sendOverMailConfig.sendMail(config.getProjectConfList());
    }

    private void config() {
        config = Config.getInstance();
    }

    private void workWithProjects() {
        List<ProjectConf> projectConfList = config.getProjectConfList();
        System.out.println(projectConfList.size());
        for (ProjectConf projectConf : projectConfList) {
            threadPool.execute(new ProjectController(projectConf));
        }
    }

    private void clearDuplicateProjects() {
   /*     List<ProjectConf> projectConfList = config.getProjectConfList();
        List<ProjectConf> tmpList = new ArrayList<>();

        tmpList.add(projectConfList.get(0));
        for (ProjectConf projectConf : projectConfList) {
            for (ProjectConf conf : tmpList) {
                if (projectConf.getDataBase().equals(conf.getDataBase())) {
                    break;
                }
            }
            tmpList.add(projectConf);
        }
        config.setProjectConfList(tmpList);*/
    }
}
