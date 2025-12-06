package HEPL.reservationAdmin.classe;

import java.io.IOException;
import java.io.InputStream;

public class LecteurProtocole {

    private static final String DELIM = "##//##";

    public static String lireMessage(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;

        while ((c = is.read()) != -1) {   // lecture octet par octet
            sb.append((char) c);

            // si on détecte le délimiteur à la fin
            if (sb.length() >= DELIM.length()) {
                if (sb.substring(sb.length() - DELIM.length()).equals(DELIM)) {
                    // renvoyer le message sans le délimiteur
                    return sb.substring(0, sb.length() - DELIM.length());
                }
            }
        }

        // si -1 = la socket est fermée par le serveur avant réception
        return null;
    }
}
