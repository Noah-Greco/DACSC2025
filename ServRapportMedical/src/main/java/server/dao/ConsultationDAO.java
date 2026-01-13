package server.dao;

import server.db.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultationDAO {

    /**
     * Retourne true si le patient a AU MOINS UNE consultation avec ce médecin.
     * (consultations.doctor_id = doctorId AND consultations.patient_id = patientId)
     */
    public boolean hasConsultation(int doctorId, int patientId) throws SQLException {

        String sql = "SELECT COUNT(*) AS c FROM consultations WHERE doctor_id = ? AND patient_id = ?";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("c") > 0;
            }
        }
    }
}
