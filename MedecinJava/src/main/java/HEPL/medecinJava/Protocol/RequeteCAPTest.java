package HEPL.medecinJava.Protocol;

import ServeurGeneriqueTCP.Requete;

public class RequeteCAPTest implements Requete {

    private String message;

    public RequeteCAPTest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RequeteCAPTest{message='" + message + "'}";
    }
}
