package HEPL.medecinJava.Protocol;

import java.time.LocalDate;

public class RequeteCAPSearchConsultations extends RequeteCAP {

    private final Integer idPatient; // ou nom/prénom selon ton searchVM
    private final LocalDate date;    // date de début ou exacte, selon ton design

    public RequeteCAPSearchConsultations(Integer idPatient, LocalDate date) {
        this.idPatient = idPatient;
        this.date = date;
    }

    public Integer getIdPatient() { return idPatient; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return "RequeteCAPSearchConsultations{idPatient=" + idPatient +
                ", date=" + date + "}";
    }
}
