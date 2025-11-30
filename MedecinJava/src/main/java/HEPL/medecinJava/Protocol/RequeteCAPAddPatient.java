package HEPL.medecinJava.Protocol;

public class RequeteCAPAddPatient extends RequeteCAP {

    private final String nom;
    private final String prenom;

    public RequeteCAPAddPatient(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }

    @Override
    public String toString() {
        return "RequeteCAPAddPatient{nom='" + nom + "', prenom='" + prenom + "'}";
    }
}
