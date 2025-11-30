package ServeurGeneriqueTCP;

public class FinConnexionException extends Exception {

    private final Reponse reponse;

    public FinConnexionException() {
        super();
        this.reponse = null;
    }

    public FinConnexionException(String message) {
        super(message);
        this.reponse = null;
    }

    public FinConnexionException(Reponse reponse) {
        super();
        this.reponse = reponse;
    }

    public FinConnexionException(String message, Reponse reponse) {
        super(message);
        this.reponse = reponse;
    }

    public Reponse getReponse() {
        return reponse;
    }
}
