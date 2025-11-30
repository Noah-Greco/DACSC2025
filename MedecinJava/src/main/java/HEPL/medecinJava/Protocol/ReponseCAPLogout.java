package HEPL.medecinJava.Protocol;

public class ReponseCAPLogout extends ReponseCAP {

    public ReponseCAPLogout(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "ReponseCAPLogout{message='" + message + "'}";
    }
}
