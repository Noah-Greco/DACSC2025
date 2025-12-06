package HEPL.medecinJava.Protocol;

import java.time.LocalDate;
import java.time.LocalTime;

public class RequeteCAPUpdateConsultation extends RequeteCAP {

    private final int idConsultation;
    private final LocalDate nouvelleDate;
    private final LocalTime nouvelleHeure;
    private final Integer idPatient;// peut être null
    private final String raison;//peut être null

    public RequeteCAPUpdateConsultation(int idConsultation,
                                        LocalDate nouvelleDate,
                                        LocalTime nouvelleHeure,
                                        Integer idPatient,
                                        String raison) {
        this.idConsultation = idConsultation;
        this.nouvelleDate = nouvelleDate;
        this.nouvelleHeure = nouvelleHeure;
        this.idPatient = idPatient;
        this.raison = raison;
    }

    public int getIdConsultation() { return idConsultation; }
    public LocalDate getNouvelleDate() { return nouvelleDate; }
    public LocalTime getNouvelleHeure() { return nouvelleHeure; }
    public Integer getIdPatient() { return idPatient; }
    public String getRaison() { return raison; }

    @Override
    public String toString() {
        return "RequeteCAPUpdateConsultation{id=" + idConsultation + "}";
    }
}
