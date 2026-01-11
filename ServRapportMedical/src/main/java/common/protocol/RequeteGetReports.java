package common.protocol;

import server.generic.Requete;

public class RequeteGetReports implements Requete {
    private int patientIdFilter; // -1 = Pas de filtre (Tout récupérer)

    // Constructeur sans argument (Tout récupérer)
    public RequeteGetReports() {
        this.patientIdFilter = -1;
    }

    // Constructeur avec filtre
    public RequeteGetReports(int patientIdFilter) {
        this.patientIdFilter = patientIdFilter;
    }

    public int getPatientIdFilter() {
        return patientIdFilter;
    }
}