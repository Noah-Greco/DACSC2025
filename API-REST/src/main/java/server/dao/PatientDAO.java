package server.dao;

import server.db.DatabaseConnector;
import java.sql.*;
import java.time.LocalDate;

public class PatientDAO {
    private final Connection conn;

    public PatientDAO() throws SQLException {
        this.conn = DatabaseConnector.getConnection();
    }

    public boolean verifyPatientById(int patientId, String lastName, String firstName) throws SQLException {
        String sql = "SELECT id FROM patients WHERE id = ? AND last_name = ? AND first_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, lastName);
            ps.setString(3, firstName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    public int findPatientId(String lastName, String firstName, LocalDate birthDate) throws SQLException {
        String sql = "SELECT id FROM patients WHERE last_name = ? AND first_name = ? AND birth_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setDate(3, Date.valueOf(birthDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1; // Pas trouvé
    }

    // Crée un nouveau patient
    public int createPatient(String lastName, String firstName, LocalDate birthDate) throws SQLException {
        String sql = "INSERT INTO patients (last_name, first_name, birth_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setDate(3, Date.valueOf(birthDate));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Échec de la création du patient");
    }
}