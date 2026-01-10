package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.model.entity.Consultation;
import HEPL.medecinJava.model.viewmodel.ConsultationSearchVM;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConsultationDAO {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private Connection connection;
    private ArrayList<Consultation> consultations;

    public ConsultationDAO() {
        try
        {
            this.connection = new ConnectionBD().getConnection();
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Erreur BD : " + e.getMessage(), e);
        }

        consultations = new ArrayList<>();
    }

    public ArrayList<Consultation> getList() {
        return consultations;
    }

    public Consultation getById(int id) {
        for (Consultation consultation : consultations) {
            if (Objects.equals(consultation.getId(), id))
                return consultation;
        }
        return null;
    }

    // lecture de toutes les lignes de la table consultations
    public ArrayList<Consultation> load() {
        try {
            String sql = "SELECT * FROM consultations ORDER BY date, hour";

            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            consultations.clear();

            while (rs.next()) {
                consultations.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return consultations;
        }
    }


    // création et/ou maj
    public void save(Consultation c) {
        try {
            if (c == null)
                return;

            String sql;

            // id > 0 => UPDATE, sinon INSERT
            if (c.getId() != null && c.getId() > 0) {
                sql = "UPDATE consultations " +
                        "SET date = ?, hour = ?, doctor_id = ?, patient_id = ?, reason = ? " +
                        "WHERE id = ?";

                PreparedStatement pStmt = connection.prepareStatement(sql);
                setCommonParameters(pStmt, c);
                pStmt.setInt(6, c.getId());
                pStmt.executeUpdate();
                pStmt.close();
            } else {
                sql = "INSERT INTO consultations (date, hour, doctor_id, patient_id, reason) " +
                        "VALUES (?, ?, ?, ?, ?)";

                PreparedStatement pStmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                setCommonParameters(pStmt, c);
                pStmt.executeUpdate();

                ResultSet rs = pStmt.getGeneratedKeys();
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
                rs.close();
                pStmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // insert/update paramètres communs
    private void setCommonParameters(PreparedStatement pStmt, Consultation c) throws SQLException {

        // 1 = date (DATE)
        if (c.getDateConsultation() != null) {
            pStmt.setDate(1, Date.valueOf(c.getDateConsultation()));
        } else {
            pStmt.setNull(1, Types.DATE);
        }

        // 2 = hour (VARCHAR(10))
        if (c.getTimeConsultation() != null) {
            // LocalTime.toString() -> "HH:mm" si secondes à 0
            pStmt.setString(2, c.getTimeConsultation().toString());
        } else {
            pStmt.setNull(2, Types.VARCHAR);
        }

        // 3 = doctor_id (NOT NULL)
        pStmt.setInt(3, c.getDoctor_id());

        // 4 = patient_id (NULL possible)
        if (c.getPatient_id() != null) {
            pStmt.setInt(4, c.getPatient_id());
        } else {
            pStmt.setNull(4, Types.INTEGER);
        }

        // 5 = reason
        pStmt.setString(5, c.getReason());
    }

    // suppression
    public void delete(Consultation c) {
        if (c != null && c.getId() != null && c.getId() > 0) {
            delete(c.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null || id <= 0) return;

        try {
            String sql = "DELETE FROM consultations WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Consultation mapRow(ResultSet rs) throws SQLException {

        Consultation c = new Consultation();

        c.setId(rs.getInt("id"));

        Date d = rs.getDate("date");
        if (d != null) {
            c.setDateConsultation(d.toLocalDate());
        }

        // ----- heure stockée en VARCHAR(10) -----
        String hourStr = rs.getString("hour");
        if (hourStr != null) {
            hourStr = hourStr.trim(); // enlève espaces éventuels
            if (!hourStr.isEmpty()) {
                try {
                    // accepte "9:00" ET "09:00" ET "13:00"
                    LocalTime t = LocalTime.parse(hourStr, TIME_FMT);
                    c.setTimeConsultation(t);
                } catch (DateTimeParseException e) {
                    // log si tu veux, mais on ne plante pas tout le load()
                    Logger.getLogger(ConsultationDAO.class.getName())
                            .log(Level.WARNING, "Heure invalide en BD: " + hourStr, e);
                }
            }
        }

        c.setDoctor_id(rs.getInt("doctor_id"));

        // patient_id peut être NULL
        Integer pid = (Integer) rs.getObject("patient_id");
        c.setPatient_id(pid);

        c.setReason(rs.getString("reason"));

        return c;
    }


    public ArrayList<Consultation> load(ConsultationSearchVM searchVM) {
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT c.id, c.date, c.hour, " +
                            "       c.doctor_id, c.patient_id, c.reason " +
                            "FROM consultations c " +
                            "JOIN doctors d ON c.doctor_id = d.id " +
                            "WHERE 1=1"
            );

            if (searchVM != null) {

                if (searchVM.getId() != null) {
                    sql.append(" AND c.id = ?");
                }
                if (searchVM.getDoctorId() != null) {
                    sql.append(" AND c.doctor_id = ?");
                }
                if (searchVM.getPatientId() != null) {
                    sql.append(" AND c.patient_id = ?");
                }

                // FILTRE DATE
                if (searchVM.getDateConsultation() != null) {
                    sql.append(" AND c.date >= ?");
                }

                // FILTRE HEURE : on CAST pour comparer en TIME et pas en VARCHAR
                if (searchVM.getTimeConsultation() != null) {
                    sql.append(" AND CAST(c.hour AS TIME) >= ?");
                }
                if (searchVM.getTimeConsultationTo() != null) {
                    sql.append(" AND CAST(c.hour AS TIME) <= ?");
                }

                // FILTRE MOTIF (LIKE)
                if (searchVM.getReason() != null && !searchVM.getReason().isEmpty()) {
                    sql.append(" AND c.reason LIKE ?");
                }
            }

            sql.append(" ORDER BY c.date, c.hour");

            PreparedStatement stmt = connection.prepareStatement(sql.toString());

            int index = 1;
            if (searchVM != null) {

                if (searchVM.getId() != null) {
                    stmt.setInt(index++, searchVM.getId());
                }
                if (searchVM.getDoctorId() != null) {
                    stmt.setInt(index++, searchVM.getDoctorId());
                }
                if (searchVM.getPatientId() != null) {
                    stmt.setInt(index++, searchVM.getPatientId());
                }

                if (searchVM.getDateConsultation() != null) {
                    stmt.setDate(index++, Date.valueOf(searchVM.getDateConsultation()));
                }

                if (searchVM.getTimeConsultation() != null) {
                    stmt.setString(index++, searchVM.getTimeConsultation().toString());
                }
                if (searchVM.getTimeConsultationTo() != null) {
                    stmt.setString(index++, searchVM.getTimeConsultationTo().toString());
                }

                if (searchVM.getReason() != null && !searchVM.getReason().isEmpty()) {
                    stmt.setString(index++, searchVM.getReason() + "%");
                }
            }

            ResultSet rs = stmt.executeQuery();
            consultations.clear();

            while (rs.next()) {
                consultations.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return consultations;
        }
    }
    public boolean addConsultations(int doctorId,
                                    LocalDate date,
                                    LocalTime startTime,
                                    int durationMinutes,
                                    int count) throws SQLException {

        if (count <= 0 || durationMinutes <= 0) return false;

        LocalTime endOfDay = LocalTime.of(17, 0);
        LocalTime current = startTime;

        for (int i = 0; i < count; i++) {
            LocalTime finConsult = current.plusMinutes(durationMinutes);

            // Si on dépasse 17h00, on considère que la série est invalide
            if (finConsult.isAfter(endOfDay)) {
                return false;
            }

            Consultation c = new Consultation();
            c.setDateConsultation(date);
            c.setTimeConsultation(current);
            c.setDoctor_id(doctorId);
            c.setPatient_id(null);  // au départ, pas de patient
            c.setReason(null);      // pas encore de motif

            save(c); // INSERT via la méthode existante

            current = finConsult;
        }
        return true;
    }
    public boolean updateConsultation(int idConsultation,
                                      LocalDate nouvelleDate,
                                      LocalTime nouvelleHeure,
                                      Integer patientId,
                                      String raison) throws SQLException {

        Consultation c = getById(idConsultation);
        if (c == null) {
            return false;
        }

        if (nouvelleDate != null) {
            c.setDateConsultation(nouvelleDate);
        }
        if (nouvelleHeure != null) {
            c.setTimeConsultation(nouvelleHeure);
        }
        if (patientId != null) {
            c.setPatient_id(patientId);
        }
        if (raison != null) {
            c.setReason(raison);
        }

        save(c); // UPDATE via la méthode existante
        return true;
    }

    public ArrayList<Consultation> searchConsultations(Integer patientId,
                                                       LocalDate date) throws SQLException {

        ConsultationSearchVM vm = new ConsultationSearchVM();
        vm.setPatientId(patientId);
        vm.setDateConsultation(date);

        return load(vm);
    }

    public boolean deleteConsultation(int id) throws SQLException {
        Consultation c = getById(id);
        if (c == null) {
            return false;
        }
        delete(c); // utilise la méthode existante
        return true;
    }

}
