package HEPL.medecinJava.Protocol;

public class ReponseCAPDeleteConsultation extends ReponseCAP {

    private final boolean ok;

    public ReponseCAPDeleteConsultation(boolean ok, String message) {
        super(message);
        this.ok = ok;
    }

    public boolean isOk() { return ok; }

    @Override
    public String toString() {
        return "ReponseCAPDeleteConsultation{ok=" + ok +
                ", message='" + message + "'}";
    }
}
