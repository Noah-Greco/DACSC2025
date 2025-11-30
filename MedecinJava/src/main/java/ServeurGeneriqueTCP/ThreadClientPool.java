package ServeurGeneriqueTCP;

import java.io.*;
import java.net.Socket;

public class ThreadClientPool extends ThreadClient {

    private final FileAttente fileAttente;

    public ThreadClientPool(Protocole protocole,
                            ThreadGroup groupe,
                            Logger logger,
                            FileAttente fileAttente) throws IOException {
        super(protocole, groupe, logger);
        this.fileAttente = fileAttente;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1. Récupérer une socket dans la file (bloquant)
                csocket = fileAttente.retirerSocket();
                logger.Trace("Socket prise dans la file : " + csocket.getRemoteSocketAddress());

                ObjectOutputStream oos = null;
                ObjectInputStream ois = null;

                try {
                    // 2. Création des flux objet
                    ois = new ObjectInputStream(csocket.getInputStream());
                    oos = new ObjectOutputStream(csocket.getOutputStream());

                    // 3. SERVEUR DE REQUÊTES : UNE SEULE REQUÊTE PAR CONNEXION
                    Requete requete = (Requete) ois.readObject();
                    Reponse reponse = protocole.TraiteRequete(requete, csocket);
                    oos.writeObject(reponse);
                    oos.flush();
                }
                catch (FinConnexionException e) {
                    if (oos != null && e.getReponse() != null) {
                        try {
                            oos.writeObject(e.getReponse());
                            oos.flush();
                        } catch (IOException ioEx) {
                            logger.Trace("Erreur I/O lors de l'envoi de la réponse de fin : " + ioEx.getMessage());
                        }
                    }
                }

                catch (ClassNotFoundException e) {
                    logger.Trace("Classe inconnue : " + e.getMessage());
                }
                catch (IOException e) {
                    logger.Trace("Erreur I/O : " + e.getMessage());
                }
                finally {
                    try { csocket.close(); } catch (IOException ignored) {}
                    logger.Trace("Connexion terminée (serveur de requêtes).");
                }
            }
            catch (InterruptedException e) {
                logger.Trace("ThreadClientPool interrompu, arrêt du thread.");
                return; // on sort proprement de la boucle -> fin du thread
            }
        }
    }
}
