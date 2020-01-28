package Pools;

import Base.Config;
import lombok.Data;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.nlab.smtp.pool.SmtpConnectionPool;
import org.nlab.smtp.transport.connection.ClosableSmtpConnection;
import org.nlab.smtp.transport.factory.SmtpConnectionFactories;
import org.nlab.smtp.transport.factory.SmtpConnectionFactory;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Data
public class MailConnectionPool {

    public static SmtpConnectionFactory factory;
    public static SmtpConnectionPool smtpConnectionPool;

    public MailConnectionPool() {
        System.out.println("Подключаемся к mail серверу");
        initSessionsPool();
    }

    private void initSessionsPool() {
        Config instance = Config.getInstance();
        Properties prop = System.getProperties();

        prop.put("mail.smtp.host", instance.getMailServer()); //optional, defined in SMTPTransport
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", instance.getMailPort()); // default port 25
        prop.put("mail.smtp.starttls.enable", "true");

        System.out.println("login " + instance.getMailUser());
        //  System.out.println("password "+ instance.getMailPassword());

        Session session = Session.getInstance(prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(instance.getMailUser(), instance.getMailPassword());
                    }
                });

        factory = SmtpConnectionFactories.newSmtpFactory(session);
        smtpConnectionPool = new SmtpConnectionPool(factory);
        smtpConnectionPool.setMaxIdle(5);
        smtpConnectionPool.setMaxTotal(10);
    }

    public final ClosableSmtpConnection getConnection() {
        try {
            while (smtpConnectionPool.getMaxTotal() <= smtpConnectionPool.getNumActive()) {
                wait(1000);
                System.out.println("waiting for mail session");
            }
            return smtpConnectionPool.borrowObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public final void returnConnection(ClosableSmtpConnection closableSmtpConnection) {
        if (closableSmtpConnection == null) {
            System.err.println("Returning NULL to pool!!!");
            return;
        }
        try {
            smtpConnectionPool.returnObject(closableSmtpConnection);
        } catch (Exception ex) {
        }
    }

}
