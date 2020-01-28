package Mail;

import Base.Config;
import FileCreators.HTMLCreator;
import Pools.SqlConnectionPool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
//не использовать пока не разберусь с загрузкой в Драйв
public class SendOverSql implements MailService{
    @Deprecated
    public void sendMail() {
        final SqlConnectionPool sqlConnectionPool = new SqlConnectionPool();
        final Connection connection = sqlConnectionPool.getConnection();

        try {
            final Statement statement = connection.createStatement();
            String htmlPage = new HTMLCreator().readHtml();
            htmlPage = "\'" + htmlPage + "\'";
            String recepientsList = readRecepientsList();
            String SQL =
                    "EXEC msdb.dbo.sp_send_dbmail\n" +
                            "@profile_name = 'sqlmail',\n" +
                            "@recipients =" +recepientsList+",\n" +
                            "@subject = 'статистика использования',\n" +
                            "@body = " +htmlPage +",\n" +
                            "@importance = 'HIGH',\n" +
                            "@file_attachments = '\\\\storage\\\\users\\\\y.egorov\\\\1.csv',\n" +
                            "@body_format ='HTML'";
            final boolean execute = statement.execute(SQL);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readRecepientsList(){
        StringBuilder list = new StringBuilder("\'");
        Config instance = Config.getInstance();
        List<String> recipients = instance.getRecipientsForAll();
        for (String recipient : recipients) {
            list.append(recipient+";");
        }
        list.append("\'");
        return list.toString();
    }
}
