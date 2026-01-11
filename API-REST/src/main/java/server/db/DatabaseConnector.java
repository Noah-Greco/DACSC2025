package server.db;

import common.config.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static Connection connection = null;

    private DatabaseConnector() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                final String DB_HOST = ConfigLoader.getProperty("DB_HOST");
                final String DB_NAME = ConfigLoader.getProperty("DB_NAME");
                final String DB_USER = ConfigLoader.getProperty("DB_USER");
                final String DB_PASS = ConfigLoader.getProperty("DB_PASS");
                final String JDBC_URL = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME;

                connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
                System.out.println("Connexion à la base de données réussie. (" + DB_HOST + "/" + DB_NAME + ")");

            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver non trouvé", e);
            }
        }
        return connection;
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                System.out.println("[DB] Fermeture de la connexion à la base de données...");
                connection.close();
                connection = null;
                System.out.println("[DB] Connexion fermée.");
            } catch (SQLException e) {
                System.err.println("[DB ERROR] Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
}