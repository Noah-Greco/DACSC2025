package server.generic;
import java.io.IOException;
import java.net.Socket;

public class ThreadClientDemande extends ThreadClient { // ThreadClient est défini p.26
    public ThreadClientDemande(Protocole protocole, Socket csocket, Logger logger) throws IOException {
        super(protocole, csocket, logger);
    }

    @Override
    public void run() {
        logger.Trace("TH Client (Demande) démarre...");
        super.run(); // Exécute la boucle de lecture définie dans le parent (p.26)
        logger.Trace("TH Client (Demande) se termine.");
    }
}