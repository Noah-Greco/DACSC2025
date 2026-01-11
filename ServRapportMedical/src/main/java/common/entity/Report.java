package common.entity;

import java.util.Date;

public class Report implements Entity {
    private int id;
    private int doctorId;
    private int patientId;
    private Date date;
    private String description;

    public Report(int id, int doctorId, int patientId, Date date, String description) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.date = date;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public int getDoctorId() { return doctorId; }
    public int getPatientId() { return patientId; }
    public Date getDate() { return date; }
    public String getDescription() { return description; }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public String toString() {
        return "Rapport " + id + " (Patient " + patientId + ")";
    }
}