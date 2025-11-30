package HEPL.medecinJava.Protocol;

import ServeurGeneriqueTCP.Reponse;

public class ReponseCAPTest implements Reponse {

    private String message;

    public ReponseCAPTest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ReponseCAPTest{message='" + message + "'}";
    }
}
