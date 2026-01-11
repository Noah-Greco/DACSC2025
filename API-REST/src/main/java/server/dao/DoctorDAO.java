package server.dao;

import common.entity.Doctor;
import server.db.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {
    private final Connection conn;

    public DoctorDAO() throws SQLException {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Doctor> searchDoctors(String name, String specialty) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT d.id, d.specialty_id, d.last_name, d.first_name " +
                        "FROM doctors d " +
                        "JOIN specialties s ON s.id = d.specialty_id " +
                        "WHERE 1=1"
        );

        if (name != null && !name.isBlank()) sql.append(" AND d.last_name LIKE ?");
        if (specialty != null && !specialty.isBlank()) sql.append(" AND s.name LIKE ?");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (name != null && !name.isBlank()) ps.setString(idx++, "%" + name + "%");
            if (specialty != null && !specialty.isBlank()) ps.setString(idx++, "%" + specialty + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                            rs.getInt("id"),
                            rs.getInt("specialty_id"),
                            rs.getString("last_name"),
                            rs.getString("first_name")
                    ));
                }
            }
        }
        return doctors;
    }


    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT id, specialty_id, last_name, first_name  FROM doctors";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getInt("id"),
                        rs.getInt("specialty_id"),
                        rs.getString("last_name"),
                        rs.getString("first_name")
                ));
            }
        }
        return doctors;
    }
}