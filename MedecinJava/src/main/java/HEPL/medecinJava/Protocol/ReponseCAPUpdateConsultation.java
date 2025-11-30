package HEPL.medecinJava.Protocol;

public class ReponseCAPUpdateConsultation extends ReponseCAP {

    private final boolean ok;

    public ReponseCAPUpdateConsultation(boolean ok, String message) {
        super(message);
        this.ok = ok;
    }

    public boolean isOk() { return ok; }

    @Override
    public String toString() {
        return "ReponseCAPUpdateConsultation{ok=" + ok +
                ", message='" + message + "'}";
    }
}
