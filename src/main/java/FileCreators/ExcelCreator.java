package FileCreators;

import DTO.Statistic;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelCreator {
    int rownum =0;
    public void converToExcel(List<Statistic> statisticList){
        try {
            String filename = "statistic.xls" ;
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("DistributorStatistic");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

            HSSFRow rowhead = sheet.createRow((short)rownum++);
            final List<String> characteristics = new ArrayList(statisticList.get(0).listElements().values());

            for (int i = 0; i < characteristics.size(); i++) {
                rowhead.createCell(i).setCellValue(characteristics.get(i));
            }
            for (Statistic statistic : statisticList) {
                HSSFRow row = sheet.createRow((short) rownum++);
                List<String> listOfElements = new ArrayList<>(statistic.listElements().keySet());
                for (int i = 0; i < listOfElements.size(); i++) {
                   row.createCell(i).setCellValue(listOfElements.get(i));
                }
            }
            File file = new File(filename);
            workbook.write(file);
            workbook.close();
        } catch ( Exception ex ) {
            System.out.println(ex);
        }

    }

}
