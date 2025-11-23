package HEPL.medecinJava.model.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Consultation implements Serializable {
    private int id;
    private int doctor_id;
    private int patient_id;
    private LocalDate dateConsultation;
    private LocalTime timeConsultation;
    private String reason;

    public Consultation(){}

    public Consultation(int doctor_id, int patient_id, LocalDate dateConsultation, LocalTime timeConsultation, String reason)
    {
        this.doctor_id = doctor_id;
        this.patient_id = patient_id;
        this.dateConsultation = dateConsultation;
        this.timeConsultation = timeConsultation;
        this.reason = reason;
    }

    public Consultation(int id, int doctor_id, int  patient_id, LocalDate dateConsultation, LocalTime timeConsultation, String reason)
    {
        this(doctor_id, patient_id, dateConsultation, timeConsultation, reason);
        this.id = id;
    }

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    public int getDoctor_id()
    {
        return doctor_id;
    }

    public void setDoctor_id(int doctor_id)
    {
        this.doctor_id = doctor_id;
    }

    public int getPatient_id()
    {
        return patient_id;
    }

    public void setPatient_id(int patient_id)
    {
        this.patient_id = patient_id;
    }

    public LocalDate getDateConsultation()
    {
        return dateConsultation;
    }

    public void setDateConsultation(LocalDate dateConsultation)
    {
        this.dateConsultation = dateConsultation;
    }

    public LocalTime getTimeConsultation()
    {
        return timeConsultation;
    }

    public void setTimeConsultation(LocalTime timeConsultation)
    {
        this.timeConsultation = timeConsultation;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    @Override
    public String toString()
    {
        return "Infos Consultation : \n\t\tid = " + this.id + "\n\t\tId Docteur =  " + this.doctor_id +
                "\n\t\tId Patient = " + this.patient_id + "\n\t\tDate Consultation =  " + this.dateConsultation
                + "\n\t\tHeure Consultation =  " + this.timeConsultation + "\n\t\tRaison =  " + this.reason;
    }
}
