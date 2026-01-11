package common.protocol;

import server.generic.Reponse;

public class ReponseLogout implements Reponse {
    private boolean success;
    private String message;

    public ReponseLogout(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public String getMessage() { return message; }
}