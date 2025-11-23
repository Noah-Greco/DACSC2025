package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.model.entity.Patient;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDAO {

    private ConnectionBD connectionBD;
    private ArrayList<Patient> patients;

    public PatientDAO() {
        connectionBD = new ConnectionBD();   // comme ConnectDB dans le cours
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

            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql);
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
        try {
            if (p == null) return;

            String sql;

            if (p.getId() > 0) {
                // UPDATE
                sql = "UPDATE patients SET first_name = ?, last_name = ?, birth_date = ? " +
                        "WHERE id = ?";

                PreparedStatement pStmt = connectionBD.getConnection().prepareStatement(sql);
                setCommonParameters(pStmt, p);
                pStmt.setInt(4, p.getId());

                pStmt.executeUpdate();
                pStmt.close();

            } else {
                // INSERT
                sql = "INSERT INTO patients (first_name, last_name, birth_date) " +
                        "VALUES (?, ?, ?)";

                PreparedStatement pStmt = connectionBD.getConnection()
                        .prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

                setCommonParameters(pStmt, p);
                pStmt.executeUpdate();

                ResultSet rs = pStmt.getGeneratedKeys();
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }

                rs.close();
                pStmt.close();
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
            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql);
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
}
