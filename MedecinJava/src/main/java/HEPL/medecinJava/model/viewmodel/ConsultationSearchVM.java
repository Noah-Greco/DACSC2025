package HEPL.medecinJava.model.viewmodel;

import java.time.LocalDate;
import java.time.LocalTime;

public class ConsultationSearchVM {
    private Integer id;
    private Integer doctor_id;
    private Integer patient_id;
    private LocalDate dateConsultation;
    private LocalTime timeConsultation;
    private LocalTime timeConsultationTo;
    private String reason;

    public ConsultationSearchVM() {
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getDoctorId()
    {
        return doctor_id;
    }

    public void setDoctorId(Integer doctor_id)
    {
        this.doctor_id = doctor_id;
    }

    public Integer getPatientId()
    {
        return patient_id;
    }

    public void setPatientId(Integer patient_id)
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

    public LocalTime getTimeConsultationTo()
    {
        return timeConsultationTo;
    }

    public void setTimeConsultationTo(LocalTime timeConsultationTo)
    {
        this.timeConsultationTo = timeConsultationTo;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
