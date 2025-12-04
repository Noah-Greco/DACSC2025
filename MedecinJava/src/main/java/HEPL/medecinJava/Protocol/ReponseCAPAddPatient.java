package HEPL.medecinJava.Protocol;

import java.io.Serializable;

public class ReponseCAPAddPatient extends ReponseCAP implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean ok;
    private final int idPatient;

    public ReponseCAPAddPatient(boolean ok, int idPatient, String message) {
        super(message);
        this.ok = ok;
        this.idPatient = idPatient;
    }

    public boolean isOk() {
        return ok;
    }

    public int getIdPatient() {
        return idPatient;
    }

    @Override
    public String toString() {
        return "ReponseCAPAddPatient{ok=" + ok +
                ", idPatient=" + idPatient +
                ", message='" + message + "'}";
    }
}
