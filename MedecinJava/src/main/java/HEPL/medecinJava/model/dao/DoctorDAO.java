package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.model.entity.Doctor;

import HEPL.medecinJava.model.viewmodel.DoctorSearchVM;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoctorDAO {

    private ConnectionBD connectionBD;
    private ArrayList<Doctor> doctors;

    public DoctorDAO() {
        connectionBD = new ConnectionBD();        // même principe que ConnectDB dans le cours
        doctors = new ArrayList<>();
    }

    public ArrayList<Doctor> getList() {
        return doctors;
    }

    public Doctor getById(Integer id) {
        for (Doctor d : doctors) {
            if (Objects.equals(d.getId(), id)) {
                return d;
            }
        }
        return null;
    }

    // ---------------------- READ : charger tous les docteurs ----------------------

    public ArrayList<Doctor> load() {
        try {
            String sql = "SELECT id, specialty_id, last_name, first_name FROM doctors ORDER BY last_name";

            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            doctors.clear();

            while (rs.next()) {
                doctors.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return doctors;
        }
    }

    // ---------------------- CREATE + UPDATE : save() ----------------------

    public void save(Doctor d) {
        try {
            if (d == null) return;

            String sql;

            if (d.getId() > 0) {
                // UPDATE
                sql = "UPDATE doctors SET specialty_id = ?, last_name = ?, first_name = ? WHERE id = ?";

                PreparedStatement pStmt = connectionBD.getConnection().prepareStatement(sql);
                setCommonParameters(pStmt, d);
                pStmt.setInt(4, d.getId());

                pStmt.executeUpdate();
                pStmt.close();

            } else {
                // INSERT
                sql = "INSERT INTO doctors (specialty_id, last_name, first_name) VALUES (?, ?, ?)";

                PreparedStatement pStmt = connectionBD.getConnection()
                        .prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

                setCommonParameters(pStmt, d);
                pStmt.executeUpdate();

                ResultSet rs = pStmt.getGeneratedKeys();
                if (rs.next()) {
                    d.setId(rs.getInt(1));
                }

                rs.close();
                pStmt.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // paramètres communs pour INSERT et UPDATE
    private void setCommonParameters(PreparedStatement pStmt, Doctor d) throws SQLException {
        pStmt.setInt(1, d.getSpecialtyId());
        pStmt.setString(2, d.getLastName());
        pStmt.setString(3, d.getFirstName());
    }

    // ---------------------- DELETE ----------------------

    public void delete(Doctor d) {
        if (d != null && d.getId() > 0) {
            delete(d.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM doctors WHERE id = ?";
            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ---------------------- mapRow() : ResultSet → Doctor ----------------------

    private Doctor mapRow(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getInt("specialty_id"),
                rs.getString("last_name"),
                rs.getString("first_name")
        );
    }

    public ArrayList<Doctor> load(DoctorSearchVM searchVM) {
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT id, specialty_id, last_name, first_name FROM doctors WHERE 1=1"
            );

            if (searchVM != null) {
                if (searchVM.getId() != null) {
                    sql.append(" AND id = ?");
                }
                if (searchVM.getSpecialtyId() != null) {
                    sql.append(" AND specialty_id = ?");
                }
                if (searchVM.getLastName() != null && !searchVM.getLastName().isEmpty()) {
                    sql.append(" AND last_name LIKE ?");
                }
            }

            sql.append(" ORDER BY last_name, first_name");

            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql.toString());

            int index = 1;
            if (searchVM != null) {
                if (searchVM.getId() != null) {
                    stmt.setInt(index++, searchVM.getId());
                }
                if (searchVM.getSpecialtyId() != null) {
                    stmt.setInt(index++, searchVM.getSpecialtyId());
                }
                if (searchVM.getLastName() != null && !searchVM.getLastName().isEmpty()) {
                    stmt.setString(index++, searchVM.getLastName() + "%");
                }
            }

            ResultSet rs = stmt.executeQuery();
            doctors.clear();

            while (rs.next()) {
                doctors.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return doctors;
        }
    }

}
