package common.protocol;

import server.generic.Reponse;

public class ReponseLogin implements Reponse {
    private boolean valide;
    private String message;
    private byte[] sessionKey;
    public ReponseLogin(boolean valide, String message, byte[] sessionKey) {
        this.valide = valide;
        this.message = message;
        this.sessionKey = sessionKey;
    }
    public ReponseLogin(boolean valide, String message) {
        this(valide, message, null);
    }

    public boolean isValide() { return valide; }
    public String getMessage() { return message; }
    public byte[] getSessionKey() { return sessionKey; }
}