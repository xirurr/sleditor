package SQL;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.Connection;
import java.util.Properties;

public class SqlConnectionPool {

    public static GenericObjectPool connectionPool;

    public SqlConnectionPool() {
    }

    public SqlConnectionPool(String dbURL) {
        System.out.println("connect with integrated security");
        initISConnectionPool(dbURL);
    }

    public SqlConnectionPool(String dbURL, String dbUser, String dbPswd) {
        System.out.println("connect with login/password");
        initConnectionPool(dbURL, dbUser, dbPswd);
    }

    public void initConnectionPool(String dbURL, String dbUser, String dbPswd) {
        System.out.println("connection over "+dbURL+" "+dbUser+" "+dbPswd);
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            connectionPool = new GenericObjectPool(null);

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbURL, dbUser, dbPswd);

            //создаем PoolableConnectionFactory
            new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1", false, true);

            new PoolingDataSource(connectionPool);
            connectionPool.setMaxIdle(20);//устанавливаем максимальное кол-во простаивающих соединений
            connectionPool.setMaxActive(20);//устанавилваем макс. кол-во активных соединений
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initISConnectionPool(String dbURL) {
        System.out.println("connection over "+dbURL);
        prepareAuthDll();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            final Properties properties = new Properties();

            properties.setProperty("integratedSecurity", "true");

            connectionPool = new GenericObjectPool(null);//создаем GenericObjectPool

            //создаем connection factory ("фабрика соединений" - объект который будет создавать соединения)
            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbURL, properties);

            //создаем PoolableConnectionFactory
            new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1", false, true);

            new PoolingDataSource(connectionPool);
            connectionPool.setMaxIdle(20);//устанавливаем максимальное кол-во простаивающих соединений
            connectionPool.setMaxActive(300);//устанавилваем макс. кол-во активных соединений
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final Connection getConnection() {
        try {
            if (connectionPool.getMaxActive() <= connectionPool.getNumActive()) {
                System.err.println("Connections limit is over!!!");
            }
            Connection con = (Connection) connectionPool.borrowObject();

            return con;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Функция возвращения connection в пул НЕ ЗАБУДЬ!!!!!!!!!!!!!!!!
     */
    public final void returnConnection(Connection con) {
        if (con == null) {
            System.err.println("Returning NULL to pool!!!");
            return;
        }
        try {
            connectionPool.returnObject(con);
        } catch (Exception ex) {
        }
    }

    private void prepareAuthDll() {
     //   Runtime.getRuntime().load("\\sqljdbc_auth.dll");
    }
}
