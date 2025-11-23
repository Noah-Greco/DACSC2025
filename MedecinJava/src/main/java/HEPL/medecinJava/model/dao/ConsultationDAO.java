package HEPL.medecinJava.model.dao;

import HEPL.medecinJava.model.entity.Consultation;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsultationDAO {
    private ConnectionBD connectionBD;
    private ArrayList<Consultation> consultations;

    public ConsultationDAO()
    {
        connectionBD = new ConnectionBD();
        consultations = new ArrayList<>();
    }

    public ArrayList<Consultation> getList()
    {
        return consultations;
    }

    public Consultation getById(int id)
    {
        for(Consultation consultation : consultations)
        {
            if(Objects.equals(consultation.getId(), id))
                return consultation;
        }
        return null;
    }

    //lecture de toute les lignes de la table consultations
    public ArrayList<Consultation> load()
    {
        try
        {
            String Sql = "select * FROM Consultation order by date_consulation, time";

            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(Sql);
            ResultSet rs = stmt.executeQuery();

            consultations.clear();

            while(rs.next())
            {
                consultations.add(mapRow(rs));
            }

            stmt.close();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            return consultations;
        }
    }

    //création et ou maj
    public void save(Consultation c)
    {
        try
        {
            if(c == null)
                return;

            String Sql;

            if(c.getId() != null)
            {
                Sql = "UPDATE consultations SET date_consulation=?, heure=?, id_medecin=?, id_patient=?, " +
                        "motif = ? WHERE id=?";

                PreparedStatement pStmt = connectionBD.getConnection().prepareStatement(Sql);
                setCommonParameters(pStmt, c);
                pStmt.setInt(6, c.getId());
                pStmt.executeUpdate();
                pStmt.close();
            }
            else
            {
                Sql = "INSERT into consultations (date_consulation, heure, id_medecin, id_patient, motif)" +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pStmt = connectionBD.getConnection().prepareStatement(Sql, PreparedStatement.RETURN_GENERATED_KEYS);
                setCommonParameters(pStmt, c);
                pStmt.executeUpdate();

                ResultSet rs = pStmt.getGeneratedKeys();

                if(rs.next())
                {
                    c.setId(rs.getInt(1));
                }
                rs.close();
                pStmt.close();
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //insert/update param commun
    private void setCommonParameters(PreparedStatement pStmt, Consultation c) throws  SQLException
    {
        //LocalDate
        if(c.getDateConsultation() != null)
        {
            pStmt.setDate(1, Date.valueOf(c.getDateConsultation()));
        }
        else
        {
            pStmt.setDate(1, null);
        }

        //LocalTime
        if(c.getTimeConsultation() != null)
        {
            pStmt.setTime(2, Time.valueOf(c.getTimeConsultation()));
        }
        else
        {
            pStmt.setTime(2, null);
        }

        pStmt.setInt(3, c.getDoctor_id());
        pStmt.setInt(4, c.getPatient_id());
        pStmt.setString(5, c.getReason());
    }

    //suppression
    public void delete(Consultation c)
    {
        if(c != null && c.getId() != null)
        {
            delete(c.getId());
        }
    }

    public void delete(Integer id)
    {
        if(id == null)return;

        try
        {
            String Sql = "delete from consultations where id=?";
            PreparedStatement stmt = connectionBD.getConnection().prepareStatement(Sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //maping et resultSet de consultation
    private Consultation mapRow(ResultSet rs) throws SQLException
    {
        Consultation c = new Consultation();

        c.setId(rs.getInt("id"));

        Date d = rs.getDate("date_consulation");
        if (d != null)
        {
            c.setDateConsultation(d.toLocalDate());
        }

        Time t = rs.getTime("heure");
        if(t != null)
        {
            c.setTimeConsultation(t.toLocalTime());
        }

        c.setDoctor_id(rs.getInt("doctor_id"));
        c.setPatient_id(rs.getInt("patient_id"));
        c.setReason(rs.getString("reason"));

        return c;
    }

}
