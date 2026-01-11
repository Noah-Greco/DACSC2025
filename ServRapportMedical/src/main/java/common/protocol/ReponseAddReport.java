package common.protocol;

import server.generic.Reponse;

public class ReponseAddReport implements Reponse {
    private boolean success;
    private String message;

    public ReponseAddReport(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}