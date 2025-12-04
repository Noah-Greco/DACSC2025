package HEPL.medecinJava.Protocol;

import java.time.LocalDate;
import java.time.LocalTime;

public class RequeteCAPAddConsultation extends RequeteCAP {

    Integer idMedecin;
    private final LocalDate date;
    private final LocalTime heure;
    private final int dureeMinutes;
    private final int nbConsultationsConsecutives;

    public RequeteCAPAddConsultation(Integer idMedecin, LocalDate date,
                                     LocalTime heure,
                                     int dureeMinutes,
                                     int nbConsultationsConsecutives) {
        this.idMedecin = idMedecin;
        this.date = date;
        this.heure = heure;
        this.dureeMinutes = dureeMinutes;
        this.nbConsultationsConsecutives = nbConsultationsConsecutives;
    }
    public Integer getIdMedecin() {return idMedecin;}

    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public int getDureeMinutes() { return dureeMinutes; }
    public int getNbConsultationsConsecutives() { return nbConsultationsConsecutives; }

    @Override
    public String toString() {
        return "RequeteCAPAddConsultation{" +
                "date=" + date +
                ", heure=" + heure +
                ", duree=" + dureeMinutes +
                ", nb=" + nbConsultationsConsecutives +
                '}';
    }
}
