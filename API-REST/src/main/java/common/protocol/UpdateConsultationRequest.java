package common.protocol;

import java.io.Serializable;

public class UpdateConsultationRequest implements Serializable {
    private int consultationId;
    private Integer patientId;
    private String reason;
    private String newDate;
    private String newHour;

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