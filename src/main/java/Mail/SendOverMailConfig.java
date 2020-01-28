package Mail;

import Base.Config;
import Base.ProjectConf;
import FileCreators.HTMLCreator;
import Pools.MailConnectionPool;
import Services.CurrentPath;
import com.sun.mail.smtp.SMTPTransport;
import org.nlab.smtp.pool.SmtpConnectionPool;
import org.nlab.smtp.transport.connection.ClosableSmtpConnection;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.*;

public class SendOverMailConfig implements MailService {
    ProjectConf currentProject;
    MailConnectionPool mailConnectionPool;
    public SendOverMailConfig(ProjectConf projectConf) {
        currentProject = projectConf;
    }
    public SendOverMailConfig(){

    }


    @Override
    public void sendMail() {
        Config instance = Config.getInstance();


        mailConnectionPool = new MailConnectionPool();
        ClosableSmtpConnection connection = mailConnectionPool.getConnection();
        MimeMessage msg = new MimeMessage(connection.getSession());

        try {
            msg.setFrom(new InternetAddress(instance.getMailUser()));

            List<String> recipients = instance.getRecipientsForAll();
            for (String recipient : recipients) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, false));
            }
            System.out.println("посылаем отчет на:" + Arrays.toString(msg.getAllRecipients()));

            msg.setSubject("статистика использования");

            // content
            MimeBodyPart htmlContent = new MimeBodyPart();

            String htmlPage = new HTMLCreator().readHtml();
            htmlContent.setContent(htmlPage, "text/html; charset=windows-1251");
            String path = CurrentPath.getInstance().getPath();
            MimeBodyPart csvFile = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(path+"statistic.csv");
            csvFile.setDataHandler(new DataHandler(fds));
            csvFile.setFileName(fds.getName());

            MimeBodyPart xlsFile = new MimeBodyPart();
            FileDataSource fds2 = new FileDataSource(path+"statistic.xls");
            xlsFile.setDataHandler(new DataHandler(fds2));
            xlsFile.setFileName(fds2.getName());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(htmlContent);
            mp.addBodyPart(csvFile);
            mp.addBodyPart(xlsFile);


            msg.setContent(mp);
            msg.setSentDate(new Date());


            // send
            connection.sendMessage(msg, msg.getAllRecipients());


        } catch (MessagingException |
                IOException e) {
            e.printStackTrace();
        }
        finally {
            mailConnectionPool.returnConnection(connection);
        }
    }
}
