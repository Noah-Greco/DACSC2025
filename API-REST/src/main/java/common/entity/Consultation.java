package common.entity;

import java.time.LocalDate;


public class Consultation implements Entity {

    private int id;
    private int doctorId;    // Clé étrangère vers Doctor (NON NULL)
    private Integer patientId; // Clé étrangère vers Patient (Peut être NULL)
    private LocalDate date;
    private String hour;
    private String reason;

    public Consultation(int id, int doctorId, Integer patientId, LocalDate date, String hour, String reason) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.date = date;
        this.hour = hour;
        this.reason = reason;
    }

    public Consultation(int doctorId, Integer patientId, LocalDate date, String hour, String reason) {
        this.id = 0;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.date = date;
        this.hour = hour;
        this.reason = reason;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public String getReason() {
        return reason;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }


}