package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.model.entity.Specialty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpecialtyDAO {

    private ConnectionBD connectionBD;
    private ArrayList<Specialty> specialties;

    public SpecialtyDAO() {
        connectionBD = new ConnectionBD();     // comme ConnectDB dans ton cours
        specialties = new ArrayList<>();
    }

    public ArrayList<Specialty> getList() {
        return specialties;
    }

    public Specialty getById(Integer id) {
        for (Specialty s : specialties) {
            if (Objects.equals(s.getId(), id)) {
                return s;
            }
        }
        return null;
    }

    // ---------------------- READ : charger toutes les spécialités ----------------------

    public ArrayList<Specialty> load() {
        try {
            String sql = "SELECT id, name FROM specialties ORDER BY name";

            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            specialties.clear();

            while (rs.next()) {
                specialties.add(mapRow(rs));
            }

            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(SpecialtyDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return specialties;
        }
    }

    // ---------------------- CREATE + UPDATE ----------------------

    public void save(Specialty s) {
        try {
            if (s == null) return;

            String sql;

            if (s.getId() > 0) {
                // UPDATE
                sql = "UPDATE specialties SET name = ? WHERE id = ?";

                PreparedStatement pStmt = connectionBD.getConnection().prepareStatement(sql);
                pStmt.setString(1, s.getName());
                pStmt.setInt(2, s.getId());

                pStmt.executeUpdate();
                pStmt.close();

            } else {
                // INSERT
                sql = "INSERT INTO specialties (name) VALUES (?)";

                PreparedStatement pStmt = connectionBD.getConnection()
                        .prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

                pStmt.setString(1, s.getName());
                pStmt.executeUpdate();

                ResultSet rs = pStmt.getGeneratedKeys();
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }

                rs.close();
                pStmt.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SpecialtyDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ---------------------- DELETE ----------------------

    public void delete(Specialty s) {
        if (s != null && s.getId() > 0) {
            delete(s.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM specialties WHERE id = ?";
            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();

        } catch (SQLException ex) {
            Logger.getLogger(SpecialtyDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ---------------------- Mapping ResultSet → Specialty ----------------------

    private Specialty mapRow(ResultSet rs) throws SQLException {
        return new Specialty(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
