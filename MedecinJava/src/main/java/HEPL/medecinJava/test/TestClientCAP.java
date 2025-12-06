package HEPL.medecinJava.test;

import HEPL.medecinJava.Protocol.RequeteCAPLogin;
import HEPL.medecinJava.config.ConfigConsultation;
import ServeurGeneriqueTCP.Requete;
import ServeurGeneriqueTCP.Reponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestClientCAP {

    public static void main(String[] args) {
        try {
            //Lire le port dans le même fichier properties que le serveur
            ConfigConsultation config = new ConfigConsultation("consultation.properties");
            int port = config.getPortConsultation();

            //Connexion au serveur (localhost, même port)
            try (Socket socket = new Socket("127.0.0.1", port)) {
                System.out.println("Connecté au serveur Consultation sur le port " + port);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  ois = new ObjectInputStream(socket.getInputStream());

                Requete requete = new RequeteCAPLogin(1, "1234");

                if (requete == null) {
                    throw new IllegalStateException("Tu dois instancier une vraie RequeteCAP ici.");
                }

                //Envoyer la requête
                oos.writeObject(requete);
                oos.flush();
                System.out.println("Requête envoyée : " + requete.getClass().getSimpleName());

                //Recevoir la réponse
                Object obj = ois.readObject();
                if (obj instanceof Reponse reponse) {
                    System.out.println("Réponse reçue : " + reponse.getClass().getSimpleName());
                    System.out.println("Contenu = " + reponse);
                } else {
                    System.out.println("Objet reçu inattendu : " + obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
