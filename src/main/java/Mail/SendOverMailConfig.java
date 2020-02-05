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
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SendOverMailConfig implements MailService {
    ProjectConf currentProject;
    MailConnectionPool mailConnectionPool;
    SimpleDateFormat inputDateFormat=new SimpleDateFormat ("yyyyMMdd");
    SimpleDateFormat outputDateFormat=new SimpleDateFormat ("MMM yyyy");

    public SendOverMailConfig(ProjectConf projectConf) {
        currentProject = projectConf;
    }

    public SendOverMailConfig() {

    }


    @Override
    public void sendMail() {
        Config instance = Config.getInstance();
        mailConnectionPool = new MailConnectionPool();
        ClosableSmtpConnection connection = mailConnectionPool.getConnection();
        Session session = connection.getSession();
        MimeMessage msg = new MimeMessage(session);

        try {
            final Date parse = inputDateFormat.parse(instance.getDate());
            msg.setFrom(new InternetAddress(instance.getMailUser()));
            List<String> recipients = currentProject.getRecipientsForOne();
            if (recipients.size() != 0) {
                for (String recipient : recipients) {
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, false));
                }

                System.out.println("посылаем отчет на:" + Arrays.toString(msg.getAllRecipients()));
                msg.setSubject("статистика использования по "+currentProject.getDataBase());

                MimeBodyPart htmlContent = new MimeBodyPart();
                String htmlPage = new HTMLCreator(currentProject.getDataBase()).readHtml();
                htmlContent.setContent(htmlPage, "text/html; charset=windows-1251");
                String path = CurrentPath.getInstance().getPath();

                MimeBodyPart csvFile = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(path + "\\" + currentProject.getDataBase() + "\\" + "statistic.csv");
                csvFile.setDataHandler(new DataHandler(fds));
                csvFile.setFileName(currentProject.getDataBase()+" " + outputDateFormat.format(parse)+".csv");

                MimeBodyPart xlsFile = new MimeBodyPart();
                FileDataSource fds2 = new FileDataSource(path + "\\" + currentProject.getDataBase() + "\\" + "statistic.xls");
                xlsFile.setDataHandler(new DataHandler(fds2));
                xlsFile.setFileName(currentProject.getDataBase()+" " + outputDateFormat.format(parse)+".xls");

                Multipart mp = new MimeMultipart();
                mp.addBodyPart(htmlContent);
                mp.addBodyPart(csvFile);
                mp.addBodyPart(xlsFile);
                msg.setContent(mp);
                msg.setSentDate(new Date());

                SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
                t.connect();
                t.sendMessage(msg, msg.getAllRecipients());
                System.out.println("Response: " + t.getLastServerResponse());

                t.close();
            }
            else{
                System.out.println(currentProject.getDataBase()+" не имеет адресатов, данные посылаются только супервайзеру");
            }
        } catch (MessagingException | IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            mailConnectionPool.returnConnection(connection);
        }
    }


    public void sendMail(List<ProjectConf> list) {
        Config config = Config.getInstance();
        mailConnectionPool = new MailConnectionPool();
        ClosableSmtpConnection connection = mailConnectionPool.getConnection();
        Session session = connection.getSession();
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(config.getMailUser()));
            List<String> recipients = Config.getInstance().getSupervisorRecipients();
            if (recipients.size() != 0) {
                for (String recipient : recipients) {
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, false));
                }
                System.out.println("посылаем ОБЩИЙ отчет на:" + Arrays.toString(msg.getAllRecipients()));
                msg.setSubject("статистика использования");
                String path = CurrentPath.getInstance().getPath();
                Multipart mp = new MimeMultipart();
                for (ProjectConf projectConf : list) {
                    mp = addAttachment(mp, path + "\\" + projectConf.getDataBase() + "\\" + "statistic.xls", projectConf);
                }

                msg.setContent(mp);
                msg.setSentDate(new Date());

                SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
                t.connect();
                t.sendMessage(msg, msg.getAllRecipients());
                System.out.println("Response: " + t.getLastServerResponse());

                t.close();
            }
        } catch (MessagingException | ParseException e) {
            e.printStackTrace();
        } finally {
            mailConnectionPool.returnConnection(connection);
        }
    }


    private Multipart addAttachment(Multipart multipart, String filename, ProjectConf projectConf) throws ParseException {
        DataSource source = new FileDataSource(filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        final Config instance = Config.getInstance();
        final Date parse = inputDateFormat.parse(instance.getDate());
        try {
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(projectConf.getDataBase()+" " + outputDateFormat.format(parse)+".xls");
            multipart.addBodyPart(messageBodyPart);
            return multipart;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
