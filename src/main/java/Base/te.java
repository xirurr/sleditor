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

public class te {
    public static void main(String[] args) {
        ClosableSmtpConnection connection = new MailConnectionPool().getConnection();


        MimeMessage msg = new MimeMessage(connection.getSession());

        try {
            msg.setFrom(new InternetAddress("y.egorvo@gmail.ru"));
            msg.addRecipient(Message.RecipientType.TO,new InternetAddress("xirurr@gmail.com", false));
            msg.setSubject("test of Pool");
            msg.setText("TEXT1");
            connection.sendMessage(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }



    }
}

