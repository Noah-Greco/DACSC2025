package ServeurGeneriqueTCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadServeurPool extends Thread {

    private final int port;
    private final Protocole protocole;
    private final Logger logger;
    private final int taillePool;
    private final FileAttente fileAttente;

    public ThreadServeurPool(int port,
                             Protocole protocole,
                             Logger logger,
                             int taillePool) {
        super("TH ServeurPool (port=" + port + ", protocole=" + protocole.getNom() + ")");
        this.port = port;
        this.protocole = protocole;
        this.logger = logger;
        this.taillePool = taillePool;
        this.fileAttente = new FileAttente();
    }

    @Override
    public void run() {
        try (ServerSocket ssocket = new ServerSocket(port)) {
            logger.Trace("Serveur Consultation (pool) en écoute sur le port " + port);

            // 1. Création du groupe de threads clients
            ThreadGroup groupeClients = new ThreadGroup("POOL_CLIENTS");

            // 2. Création des threads du pool
            for (int i = 0; i < taillePool; i++) {
                try {
                    ThreadClientPool th =
                            new ThreadClientPool(protocole, groupeClients, logger, fileAttente);
                    th.start();
                } catch (IOException e) {
                    logger.Trace("Erreur lors de la création d'un ThreadClientPool : " + e.getMessage());
                }
            }

            // 3. Boucle d'acceptation : serveur de requêtes
            while (true) {
                logger.Trace("Attente d'une connexion...");
                Socket csocket = ssocket.accept();
                logger.Trace("Connexion acceptée depuis " + csocket.getRemoteSocketAddress());

                // On dépose la socket dans la file pour un thread du pool
                fileAttente.deposerSocket(csocket);
            }
        }
        catch (IOException e) {
            logger.Trace("Erreur dans ThreadServeurPool : " + e.getMessage());
        }
    }
}
