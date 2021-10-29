package bsu.fpmi.chat.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Gennady Trubach on 22.05.2015.
 */
public class ConnectionManager {
    private static final String URL = "jdbc:mysql://localhost:3306/chat";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "pass";
    private static Logger logger = Logger.getLogger(ConnectionManager.class.getName());

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            logger.info("Got connection to database");
        } catch (Exception e) {
            logger.error(e);
        }
        return connection;
    }
}
