package ServeurGeneriqueTCP;
import java.io.*;
import java.net.Socket;
public abstract class ThreadClient extends Thread
{
    protected Protocole protocole;
    protected Socket csocket;
    protected Logger logger;
    private int numero;
    private static int numCourant = 1;
    public ThreadClient(Protocole protocole, Socket csocket, Logger logger) throws IOException
    {
        super("TH Client " + numCourant + " (protocole=" + protocole.getNom() + ")");
        this.protocole = protocole;
        this.csocket = csocket;
        this.logger = logger;
        this.numero = numCourant++;
    }
    public ThreadClient(Protocole protocole, ThreadGroup groupe, Logger logger) throws IOException
    {
        super(groupe,"TH Client " + numCourant + " (protocole=" + protocole.getNom() + ")");
        this.protocole = protocole;
        this.csocket = null;
        this.logger = logger;
        this.numero = numCourant++;
    }
    @Override
    public void run() {
        try {
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;

            try {
                ois = new ObjectInputStream(csocket.getInputStream());
                oos = new ObjectOutputStream(csocket.getOutputStream());

                // SERVEUR DE REQUÊTES : UNE SEULE REQUÊTE PAR CONNEXION
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
        }
        finally {
            try { csocket.close(); } catch (IOException ignored) {}
            logger.Trace("Connexion terminée (serveur de requêtes).");
        }
    }
}