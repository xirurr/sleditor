package Base;

import Pools.MailConnectionPool;
import org.nlab.smtp.pool.SmtpConnectionPool;
import org.nlab.smtp.transport.connection.ClosableSmtpConnection;
import org.nlab.smtp.transport.factory.SmtpConnectionFactoryBuilder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class te {
    public static void main(String[] args) {
        Path pathCSV = Paths.get("dddddd"+"\\"+"1.sdd");
        boolean exists = Files.exists(pathCSV);
        System.out.println(exists);
    }
}

