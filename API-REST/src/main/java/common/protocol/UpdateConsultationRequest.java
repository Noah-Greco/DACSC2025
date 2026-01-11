package common.protocol;

import java.io.Serializable;

public class UpdateConsultationRequest implements Serializable {
    private int consultationId;
    private Integer patientId; // Peut être null
    private String reason;
    private String newDate;    // Format String pour simplifier le transport
    private String newHour;

    // Constructeurs, Getters et Setters
    public UpdateConsultationRequest() {}

    public int getConsultationId() { return consultationId; }
    public void setConsultationId(int consultationId) { this.consultationId = consultationId; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNewDate() { return newDate; }
    public void setNewDate(String newDate) { this.newDate = newDate; }

    public String getNewHour() { return newHour; }
    public void setNewHour(String newHour) { this.newHour = newHour; }
}