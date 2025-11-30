package HEPL.medecinJava.Protocol;

public class ReponseCAPAddPatient extends ReponseCAP {

    private final int idPatient;
    private final boolean ok;

    public ReponseCAPAddPatient(boolean ok, int idPatient, String message) {
        super(message);
        this.ok = ok;
        this.idPatient = idPatient;
    }

    public int getIdPatient() { return idPatient; }
    public boolean isOk() { return ok; }

    @Override
    public String toString() {
        return "ReponseCAPAddPatient{ok=" + ok +
                ", idPatient=" + idPatient +
                ", message='" + message + "'}";
    }
}
