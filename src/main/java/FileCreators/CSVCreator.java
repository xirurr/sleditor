package FileCreators;

import Base.Config;
import DTO.Statistic;
import SQL.SqlConnectionPool;
import Services.CurrentPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class CSVCreator {
    private static final String NEW_LINE_SEPARATOR = "\n";
    private FileWriter fileWriter = null;
    private CSVPrinter csvFilePrinter = null;
    private Object [] FILE_HEADER = null;


    public void convertToCSV(List<Statistic> statisticList){
        FILE_HEADER =statisticList.get(0).listElements().keySet().toArray();

        CSVFormat csvFileFormat = CSVFormat.EXCEL.withRecordSeparator(NEW_LINE_SEPARATOR).withDelimiter(Config.getInstance().getDelimiter());
        try {
            //initialize FileWriter object
            String path = CurrentPath.getInstance().getPath();
            fileWriter = new FileWriter(path+"statistic.csv", Charset.forName("windows-1251"));

            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            //Create CSV file header
            csvFilePrinter.printRecord(FILE_HEADER);

            //Write a new student object list to the CSV file
            for (Statistic distributor : statisticList) {
                csvFilePrinter.printRecord(distributor.listElements().values());
            }

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
    }
}
