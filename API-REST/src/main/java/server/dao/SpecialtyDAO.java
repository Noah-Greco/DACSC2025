package server.dao;

import common.entity.Specialty;
import server.db.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecialtyDAO {
    private final Connection conn;

    public SpecialtyDAO() throws SQLException {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Specialty> getAllSpecialties() throws SQLException {
        List<Specialty> specialties = new ArrayList<>();
        String sql = "SELECT * FROM specialties";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                specialties.add(new Specialty(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        }
        return specialties;
    }
}