package server.generic;
import java.io.IOException;
import java.net.*;


public class ThreadServeurDemande extends ThreadServeur {
    public ThreadServeurDemande(int port, Protocole protocole, Logger logger) throws IOException {
        super(port, protocole, logger);
    }

    @Override
    public void run() {
        logger.Trace("Démarrage du TH Serveur (Demande)...");
        while (!this.isInterrupted()) {
            Socket csocket;
            try {
                ssocket.setSoTimeout(2000);
                csocket = ssocket.accept();
                logger.Trace("Connexion acceptée, création TH Client");
                // On lance un thread dédié pour ce client (Voir PDF 4 p.29)
                new ThreadClientDemande(protocole, csocket, logger).start();
            } catch (SocketTimeoutException ex) {
                // Time out normal pour vérifier l'interruption
            } catch (IOException ex) {
                logger.Trace("Erreur I/O");
            }
        }
        logger.Trace("TH Serveur (Demande) interrompu.");
        try { ssocket.close(); } catch (IOException ex) { logger.Trace("Erreur I/O fermeture"); }
    }
}