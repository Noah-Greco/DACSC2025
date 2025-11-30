package HEPL.medecinJava.Protocol;

public class ReponseCAPAddConsultation extends ReponseCAP {

    private final boolean ok;

    public ReponseCAPAddConsultation(boolean ok, String message) {
        super(message);
        this.ok = ok;
    }

    public boolean isOk() { return ok; }

    @Override
    public String toString() {
        return "ReponseCAPAddConsultation{ok=" + ok + ", message='" + message + "'}";
    }
}
