package server.dao;

import common.entity.Doctor;
import server.db.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorDAO {

    // On récupère le médecin JUSTE avec son login
    public Doctor getDoctorByLogin(String login) throws SQLException {

        String sql = """
        SELECT
            id,
            specialty_id,
            last_name,
            first_name,
            password,
            CONCAT(first_name, last_name) AS username
        FROM doctors
        WHERE LOWER(CONCAT(first_name, last_name)) = LOWER(?)
    """;

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setString(1, login);

            try (ResultSet rs = pStmt.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                            rs.getInt("id"),
                            rs.getInt("specialty_id"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getString("username"),   // alias calculé
                            rs.getString("password")
                    );
                } else {
                    return null;
                }
            }
        }
    }

}