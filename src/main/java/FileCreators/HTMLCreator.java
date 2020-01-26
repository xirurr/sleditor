package FileCreators;

import Base.Config;
import DTO.Statistic;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import Services.CurrentPath;
import freemarker.template.*;
import org.apache.commons.io.FileUtils;

public class HTMLCreator {

    public void convertToHMTL(List<Statistic> statisticList) {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(HTMLCreator.class, "/static/templates/");
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.getDefault());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);


        Map<String, String> header = statisticList.get(0).listElements();
        Map root = new HashMap();

        root.put("header", header);
        root.put("elements", statisticList);
        root.put("count", statisticList.size());
        root.put("fromDate", Config.getInstance().getDate());




        try {
            String path = CurrentPath.getInstance().getPath();
            Template template = cfg.getTemplate("table.ftl");
            Writer fileWriter = new FileWriter(new File(path + "statistic.html"), Charset.forName("UTF-8"));
            template.process(root, fileWriter);

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }


    }

    public String readHtml() throws IOException {
        String path = CurrentPath.getInstance().getPath();
        return FileUtils.readFileToString(new File(path+"statistic.html"), Charset.forName("UTF-8"));
    }
}
