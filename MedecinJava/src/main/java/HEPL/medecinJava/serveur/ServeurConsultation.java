package HEPL.medecinJava.serveur;

import HEPL.medecinJava.config.ConfigConsultation;
import HEPL.medecinJava.Protocol.ProtocoleCAP;
import ServeurGeneriqueTCP.*;

public class ServeurConsultation {

    public static void main(String[] args) {
        try {
            // 1. Charger la configuration
            ConfigConsultation config = new ConfigConsultation("consultation.properties");
            int port = config.getPortConsultation();
            int poolSize = config.getPoolSize();

            // 2. Créer logger + protocole
            Logger logger = new LoggerConsole();
            Protocole protocole = new ProtocoleCAP(config);

            // 3. Créer et démarrer le thread serveur en pool
            ThreadServeurPool threadServeur =
                    new ThreadServeurPool(port, protocole, logger, poolSize);

            threadServeur.start();
            System.out.println("Serveur Consultation CAP démarré sur le port " + port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
