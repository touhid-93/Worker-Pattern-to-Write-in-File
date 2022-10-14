package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Touhid
 */
public class DBConnect {

    private static final String USERNAME = "root";
    private static final String PASSWORD = "2222";
    private static final String URL = "jdbc:mysql://localhost:3306/worker_test";

    Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return connection;
        } catch (SQLException ex) {
            System.err.println("SQLException : " + ex.getMessage());
            return null;
        } catch (ClassNotFoundException ex) {
            //Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("ClassNotFoundException : " + ex.getMessage());
            return null;
        }
    }
}
