package HEPL.medecinJava.Protocol;

import java.util.List;

public class ReponseCAPSearchConsultations extends ReponseCAP {

    private final List<?> consultations; // remplace ? par ton type (ConsultationVM par ex.)

    public ReponseCAPSearchConsultations(List<?> consultations, String message) {
        super(message);
        this.consultations = consultations;
    }

    public List<?> getConsultations() {
        return consultations;
    }

    @Override
    public String toString() {
        return "ReponseCAPSearchConsultations{nb=" +
                (consultations == null ? 0 : consultations.size()) +
                ", message='" + message + "'}";
    }
}
