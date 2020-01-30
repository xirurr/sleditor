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
import java.util.*;

public class SendOverMailConfig implements MailService {
    ProjectConf currentProject;
    MailConnectionPool mailConnectionPool;

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
            msg.setFrom(new InternetAddress(instance.getMailUser()));
            List<String> recipients = currentProject.getRecipientsForOne();
            if (recipients.size() != 0) {
                for (String recipient : recipients) {
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, false));
                }
                System.out.println("посылаем отчет на:" + Arrays.toString(msg.getAllRecipients()));
                msg.setSubject("статистика использования");

                MimeBodyPart htmlContent = new MimeBodyPart();
                String htmlPage = new HTMLCreator(currentProject.getDataBase()).readHtml();
                htmlContent.setContent(htmlPage, "text/html; charset=windows-1251");
                String path = CurrentPath.getInstance().getPath();

                MimeBodyPart csvFile = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(path + "\\" + currentProject.getDataBase() + "\\" + "statistic.csv");
                csvFile.setDataHandler(new DataHandler(fds));
                csvFile.setFileName(fds.getName());

                MimeBodyPart xlsFile = new MimeBodyPart();
                FileDataSource fds2 = new FileDataSource(path + "\\" + currentProject.getDataBase() + "\\" + "statistic.xls");
                xlsFile.setDataHandler(new DataHandler(fds2));
                xlsFile.setFileName(fds2.getName());

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
        } catch (MessagingException |
                IOException e) {
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
                    MimeBodyPart xlsProjectFile = new MimeBodyPart();
                    /*FileDataSource fds = new FileDataSource(path + "\\" + projectConf.getDataBase() + "\\" + "statistic.xls");
                    xlsProjectFile.setDataHandler(new DataHandler(fds));
                    xlsProjectFile.setFileName(fds.getName());
                    mp.addBodyPart(xlsProjectFile);*/
                    mp = addAttachment(mp, path + "\\" + projectConf.getDataBase() + "\\" + "statistic.xls",projectConf);


                }

                msg.setContent(mp);
                msg.setSentDate(new Date());

                SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
                t.connect();
                t.sendMessage(msg, msg.getAllRecipients());
                System.out.println("Response: " + t.getLastServerResponse());

                t.close();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            mailConnectionPool.returnConnection(connection);
        }
    }


    private Multipart addAttachment(Multipart multipart, String filename, ProjectConf projectConf)
    {
        DataSource source = new FileDataSource(filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(projectConf.getDataBase()+".xls");
            multipart.addBodyPart(messageBodyPart);
            return multipart;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
return null;
    }
}
