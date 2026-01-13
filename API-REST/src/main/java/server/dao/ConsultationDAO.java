package server.dao;

import common.entity.Consultation;
import common.protocol.UpdateConsultationRequest;
import server.db.DatabaseConnector;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {

    private final Connection conn;

    public ConsultationDAO() throws SQLException {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Consultation> searchConsultationsRest(
            String doctorLastName,
            String specialtyName,
            LocalDate date,
            Integer patientId
    ) throws SQLException {

        List<Consultation> consultations = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.* " +
                        "FROM consultations c " +
                        "JOIN doctors d ON d.id = c.doctor_id " +
                        "JOIN specialties s ON s.id = d.specialty_id " +
                        "WHERE 1=1 "
        );

        if (patientId != null) {
            sql.append(" AND c.patient_id = ? ");
        } else {
            sql.append(" AND c.patient_id IS NULL ");
        }

        if (doctorLastName != null && !doctorLastName.isBlank()) {
            sql.append(" AND d.last_name LIKE ? ");
        }
        if (specialtyName != null && !specialtyName.isBlank()) {
            sql.append(" AND s.name LIKE ? ");
        }
        if (date != null) {
            sql.append(" AND c.date = ? ");
        }

        sql.append(" ORDER BY c.date, c.hour ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;

            if (patientId != null) ps.setInt(idx++, patientId);
            if (doctorLastName != null && !doctorLastName.isBlank()) ps.setString(idx++, "%" + doctorLastName + "%");
            if (specialtyName != null && !specialtyName.isBlank()) ps.setString(idx++, "%" + specialtyName + "%");
            if (date != null) ps.setDate(idx++, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pId = rs.getInt("patient_id");
                    int finalPatientId = rs.wasNull() ? 0 : pId;

                    consultations.add(new Consultation(
                            rs.getInt("id"),
                            rs.getInt("doctor_id"),
                            finalPatientId,
                            rs.getDate("date").toLocalDate(),
                            rs.getString("hour"),
                            rs.getString("reason")
                    ));
                }
            }
        }
        return consultations;
    }

    public List<Consultation> searchConsultation(Integer doctorId, Integer patientId, LocalDate dateStart, LocalDate dateEnd) throws SQLException {
        List<Consultation> consultations = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM consultations WHERE 1=1");

        if (patientId != null && patientId > 0) {
            sql.append(" AND patient_id = ?");
        }
        else if (doctorId != null && doctorId > 0) {
            sql.append(" AND doctor_id = ?");
        }

        if (dateStart != null) {
            sql.append(" AND date >= ?");
        }

        sql.append(" ORDER BY date, hour");

        try (PreparedStatement pStmt = this.conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (patientId != null && patientId > 0) {
                pStmt.setInt(paramIndex++, patientId);
            }
            else if (doctorId != null && doctorId > 0) {
                pStmt.setInt(paramIndex++, doctorId);
            }

            if (dateStart != null) {
                pStmt.setDate(paramIndex++, java.sql.Date.valueOf(dateStart));
            }

            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int docId = rs.getInt("doctor_id");

                    int pId = rs.getInt("patient_id");
                    int finalPatientId = rs.wasNull() ? 0 : pId;


                    String hourVal = rs.getString("hour");

                    Consultation consultation = new Consultation(
                            id,
                            docId,
                            finalPatientId,
                            rs.getDate("date").toLocalDate(),
                            hourVal,
                            rs.getString("reason")
                    );

                    consultations.add(consultation);
                }
            }
        }

        return consultations;
    }

    public int addConsultations(int doctorId, LocalDate date, String startTime, int duration, int number) throws SQLException {

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime currentTime = LocalTime.parse(startTime, timeFormatter);
        final LocalTime endTimeLimit = LocalTime.of(17, 0);

        String sql = "INSERT INTO consultations (doctor_id, patient_id, date, hour, reason) VALUES (?, NULL, ?, ?, '')";

        Connection dbConn = this.conn;

        try (PreparedStatement pStmt = dbConn.prepareStatement(sql)) {

            dbConn.setAutoCommit(false);

            int createdCount = 0;
            for (int i = 0; i < number; i++) {

                if (currentTime.isAfter(endTimeLimit) || currentTime.plusMinutes(duration).isAfter(endTimeLimit)) {
                    throw new SQLException("Impossible d'ajouter, le créneau dépasse 17:00.");
                }

                pStmt.setInt(1, doctorId);
                pStmt.setDate(2, Date.valueOf(date));
                pStmt.setString(3, currentTime.format(timeFormatter));

                pStmt.addBatch();

                currentTime = currentTime.plusMinutes(duration);
                createdCount++;
            }

            pStmt.executeBatch();
            dbConn.commit();
            return createdCount;

        } catch (SQLException e) {
            dbConn.rollback();
            System.err.println("Erreur de transaction (AddConsultations): " + e.getMessage());
            throw e;
        } finally {
            dbConn.setAutoCommit(true);
        }
    }


    public boolean updateConsultation(UpdateConsultationRequest req) throws SQLException {

        String sql;

        if (req.getPatientId() != null) {
            sql = "UPDATE consultations SET patient_id = ?, reason = ? WHERE id = ? AND patient_id IS NULL";
        } else {
            sql = "UPDATE consultations SET date = ?, hour = ? WHERE id = ?";
        }

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {

            if (req.getPatientId() != null) {
                pStmt.setInt(1, req.getPatientId());
                pStmt.setString(2, req.getReason());
                pStmt.setInt(3, req.getConsultationId());
            } else {
                pStmt.setDate(1, Date.valueOf(req.getNewDate()));
                pStmt.setString(2, req.getNewHour());
                pStmt.setInt(3, req.getConsultationId());
            }

            int affectedRows = pStmt.executeUpdate();

            if (req.getPatientId() != null && affectedRows == 0) {
                throw new SQLException("Échec de l'assignation: le créneau (ID: " + req.getConsultationId() + ") est déjà réservé.");
            }

            return affectedRows > 0;
        }
    }

    public boolean deleteConsultation(int consultationId) throws SQLException {
        String sql = "UPDATE consultations SET patient_id = NULL, reason = '' WHERE id = ?";

        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, consultationId);
            int affectedRows = pStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}