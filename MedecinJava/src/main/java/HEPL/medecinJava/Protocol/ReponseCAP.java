package HEPL.medecinJava.Protocol;

import ServeurGeneriqueTCP.Reponse;

public abstract class ReponseCAP implements Reponse {
    protected String message;

    protected ReponseCAP(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
