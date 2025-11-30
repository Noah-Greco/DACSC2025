package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.config.ConfigConsultation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionBD {
    private Connection conn;


    public ConnectionBD() throws IOException, SQLException {
        ConfigConsultation config = new ConfigConsultation("consultation.properties");

        String url = config.getDbUrl();
        String user = config.getDbUser();
        String password = config.getDbPassword();

        conn = DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() {
        return conn;
    }
}
