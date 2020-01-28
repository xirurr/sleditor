package Mail;

import Base.Config;
import FileCreators.HTMLCreator;
import Services.CurrentPath;
import com.sun.mail.smtp.SMTPTransport;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.*;

public class SendOverMailConfig implements MailService {

    @Override
    public void sendMail() {
        Config instance = Config.getInstance();
        Properties prop = System.getProperties();

        prop.put("mail.smtp.host", instance.getMailServer()); //optional, defined in SMTPTransport
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", instance.getMailPort()); // default port 25
     //   prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        System.out.println("login "+instance.getMailUser());
        System.out.println("password "+ instance.getMailPassword());

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(instance.getMailUser(), instance.getMailPassword());
                    }
                });
        Message msg = new MimeMessage(session);

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


            // Get SMTPTransport
            // SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");

            // connect
            t.connect(instance.getMailServer(), instance.getMailUser(), instance.getMailPassword());

            // send
            t.sendMessage(msg, msg.getAllRecipients());

            System.out.println("Response: " + t.getLastServerResponse());

            t.close();

        } catch (MessagingException |
                IOException e) {
            e.printStackTrace();
        }
    }
}
