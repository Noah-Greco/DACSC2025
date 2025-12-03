package HEPL.medecinJava.Protocol;

import java.time.LocalDate;

public class RequeteCAPSearchConsultations extends RequeteCAP {

    private final Integer idDoctor;
    private final LocalDate date;

    public RequeteCAPSearchConsultations(Integer idDoctor, LocalDate date) {
        this.idDoctor = idDoctor;
        this.date = date;
    }

    public Integer getIdDoctor() { return idDoctor; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return "RequeteCAPSearchConsultations{idPatient=" + idDoctor +
                ", date=" + date + "}";
    }
}
