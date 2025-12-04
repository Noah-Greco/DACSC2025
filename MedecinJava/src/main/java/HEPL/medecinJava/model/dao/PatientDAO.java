package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.model.entity.Patient;

import HEPL.medecinJava.model.viewmodel.PatientSearchVM;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDAO {

    private Connection connection;
    private ArrayList<Patient> patients;

    public PatientDAO() {
        try
        {
            this.connection = new ConnectionBD().getConnection();
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Erreur BD : " + e.getMessage(), e);
        }
        patients = new ArrayList<>();
    }

    public ArrayList<Patient> getList() {
        return patients;
    }

    public Patient getById(Integer id) {
        for (Patient p : patients) {
            if (Objects.equals(p.getId(), id)) {
                return p;
            }
        }
        return null;
    }

    // ---------------------- READ : charger tous les patients ----------------------

    public ArrayList<Patient> load() {
        try {
            String sql = "SELECT id, first_name, last_name, birth_date " +
                    "FROM patients ORDER BY last_name, first_name";

            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            patients.clear();

            while (rs.next()) {
                patients.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return patients;
        }
    }

    // ---------------------- CREATE + UPDATE : save() ----------------------

    public void save(Patient p) {
        if (p == null) return;

        try {
            Integer id = p.getId(); // peut être null pour un nouveau patient
            String sql;

            if (id != null && id > 0) {
                // ================== UPDATE ==================
                sql = "UPDATE patients SET first_name = ?, last_name = ?, birth_date = ? " +
                        "WHERE id = ?";

                try (PreparedStatement pStmt = connection.prepareStatement(sql)) {
                    setCommonParameters(pStmt, p);
                    pStmt.setInt(4, id);
                    pStmt.executeUpdate();
                }

            } else {
                // ================== INSERT ==================
                sql = "INSERT INTO patients (first_name, last_name, birth_date) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement pStmt =
                             connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    setCommonParameters(pStmt, p);
                    pStmt.executeUpdate();

                    try (ResultSet rs = pStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            p.setId(rs.getInt(1));    // récupère l’ID auto-incrémenté
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    // paramètres communs INSERT / UPDATE
    private void setCommonParameters(PreparedStatement pStmt, Patient p) throws SQLException {
        pStmt.setString(1, p.getFirstName());
        pStmt.setString(2, p.getLastName());

        if (p.getBirthDate() != null) {
            pStmt.setDate(3, Date.valueOf(p.getBirthDate()));   // LocalDate -> java.sql.Date
        } else {
            pStmt.setDate(3, null);
        }
    }

    // ---------------------- DELETE ----------------------

    public void delete(Patient p) {
        if (p != null && p.getId() > 0) {
            delete(p.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM patients WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ---------------------- mapping ResultSet -> Patient ----------------------

    private Patient mapRow(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("birth_date");

        return new Patient(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                (sqlDate != null) ? sqlDate.toLocalDate() : null
        );
    }

    public ArrayList<Patient> load(PatientSearchVM searchVM) {
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT id, first_name, last_name, birth_date FROM patients WHERE 1=1"
            );

            if (searchVM != null) {
                if (searchVM.getId() != null) {
                    sql.append(" AND id = ?");
                }
                if (searchVM.getLastName() != null && !searchVM.getLastName().isEmpty()) {
                    sql.append(" AND last_name LIKE ?");
                }
                if (searchVM.getFirstName() != null && !searchVM.getFirstName().isEmpty()) {
                    sql.append(" AND first_name LIKE ?");
                }
            }

            sql.append(" ORDER BY last_name, first_name");

            PreparedStatement stmt = connection.prepareStatement(sql.toString());

            int index = 1;
            if (searchVM != null) {
                if (searchVM.getId() != null) {
                    stmt.setInt(index++, searchVM.getId());
                }
                if (searchVM.getLastName() != null && !searchVM.getLastName().isEmpty()) {
                    stmt.setString(index++, searchVM.getLastName() + "%");
                }
                if (searchVM.getFirstName() != null && !searchVM.getFirstName().isEmpty()) {
                    stmt.setString(index++, searchVM.getFirstName() + "%");
                }
            }

            ResultSet rs = stmt.executeQuery();
            patients.clear();

            while (rs.next()) {
                patients.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return patients;
        }
    }

    /**
     * Ajoute un patient avec nom / prénom et renvoie son id.
     * birth_date est laissée à null.
     */
    public int addPatient(String lastName, String firstName) throws SQLException {
        Patient p = new Patient();
        p.setLastName(lastName);
        p.setFirstName(firstName);
        // p.setBirthDate(...); // si tu veux le gérer plus tard

        save(p);  // fait l'INSERT et met l'id dans p

        Integer id = p.getId();
        if (id == null) {
            throw new SQLException("Insertion patient : ID généré null");
        }
        return id;
    }



}
