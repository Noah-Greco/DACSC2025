package server;

import common.config.ConfigLoader; // <--- Import
import server.generic.*;
import server.logic.MRPSProtocol;
import java.io.IOException;

public class MainServer {
    public static void main(String[] args) {

        // 1. Lecture du port depuis le fichier de config
        int port = Integer.parseInt(ConfigLoader.getProperty("PORT_REPORT_SECURE"));

        Logger consoleLogger = message -> System.out.println("[SERVEUR] " + message);

        try {
            consoleLogger.Trace("Lecture configuration : Port=" + port);
            consoleLogger.Trace("Initialisation du protocole MRPS...");

            Protocole protocole = new MRPSProtocol(consoleLogger);

            consoleLogger.Trace("Démarrage du serveur...");

            // On lance le serveur sur le port récupéré
            ThreadServeur serveur = new ThreadServeurDemande(port, protocole, consoleLogger);
            serveur.start();

        } catch (IOException e) {
            System.err.println("Erreur au lancement : " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erreur config : Le port n'est pas un nombre valide.");
        }
    }
}