package server.dao;

import common.entity.Report;
import server.db.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public List<Report> getReportsByDoctor(int doctorId) throws SQLException {
        List<Report> reports = new ArrayList<>();

        // On sélectionne uniquement les rapports de CE médecin
        String sql = "SELECT * FROM reports WHERE doctor_id = ?";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, doctorId);

            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    Report r = new Report(
                            rs.getInt("id"),
                            rs.getInt("doctor_id"),
                            rs.getInt("patient_id"),
                            rs.getDate("date"),
                            rs.getString("description")
                    );
                    reports.add(r);
                }
            }
        }
        return reports;
    }

    // Nouvelle méthode : Récupère les rapports d'un médecin POUR UN PATIENT précis
    public List<Report> getReportsByDoctorAndPatient(int doctorId, int patientId) throws SQLException {
        List<Report> list = new ArrayList<>();

        // La clause WHERE filtre sur les deux IDs
        String sql = "SELECT * FROM reports WHERE doctor_id = ? AND patient_id = ?";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, doctorId);
            pStmt.setInt(2, patientId);

            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    Report r = new Report(
                            rs.getInt("id"),
                            rs.getInt("doctor_id"),
                            rs.getInt("patient_id"),
                            rs.getDate("date"),
                            rs.getString("description")
                    );
                    list.add(r);
                }
            }
        }
        return list;
    }

    public void insertReport(Report r) throws SQLException {
        String sql = "INSERT INTO reports (doctor_id, patient_id, date, description) VALUES (?, ?, ?, ?)";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, r.getDoctorId());
            pStmt.setInt(2, r.getPatientId());
            // Conversion java.util.Date -> java.sql.Date
            pStmt.setDate(3, new java.sql.Date(r.getDate().getTime()));
            pStmt.setString(4, r.getDescription());

            pStmt.executeUpdate();
        }
    }

    public void updateReport(Report r) throws SQLException {
        // On ne modifie que la description et la date, pas les IDs (sauf pour vérif)
        String sql = "UPDATE reports SET description = ?, date = ? WHERE id = ? AND doctor_id = ?";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setString(1, r.getDescription());
            pStmt.setDate(2, new java.sql.Date(r.getDate().getTime()));
            pStmt.setInt(3, r.getId());
            pStmt.setInt(4, r.getDoctorId()); // Sécurité : on vérifie que c'est bien CE médecin

            int rowsAffected = pStmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Aucun rapport trouvé ou vous n'avez pas les droits.");
            }
        }
    }
}