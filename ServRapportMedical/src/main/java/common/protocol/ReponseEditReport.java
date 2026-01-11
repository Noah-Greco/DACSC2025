package common.protocol;

import server.generic.Reponse;

public class ReponseEditReport implements Reponse {
    private boolean success;
    private String message;

    public ReponseEditReport(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}