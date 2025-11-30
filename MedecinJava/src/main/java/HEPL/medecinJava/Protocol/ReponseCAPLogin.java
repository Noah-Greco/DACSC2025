package HEPL.medecinJava.Protocol;

public class ReponseCAPLogin extends ReponseCAP {

    private final boolean ok;
    // éventuellement: id du médecin, nom complet, etc.

    public ReponseCAPLogin(boolean ok, String message) {
        super(message);
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public String toString() {
        return "ReponseCAPLogin{ok=" + ok + ", message='" + message + "'}";
    }
}
