package HEPL.medecinJava.Protocol;

public class RequeteCAPLogout extends RequeteCAP {

    private final String login; // ou idMedecin si tu gères une session

    public RequeteCAPLogout(String login) {
        this.login = login;
    }

    public String getLogin() { return login; }

    @Override
    public String toString() {
        return "RequeteCAPLogout{login='" + login + "'}";
    }
}
