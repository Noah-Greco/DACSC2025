package HEPL.medecinJava.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionBD {

    private Connection conn;

    public ConnectionBD() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://<IP_VM>:3306/PourStudent?serverTimezone=UTC",
                    "Student",
                    "PassStudent1_"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }
}
