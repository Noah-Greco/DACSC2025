package HEPL.medecinJava.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigConsultation {

    private final int portConsultation;
    private final int poolSize;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public ConfigConsultation(String propertiesFile) throws IOException {
        Properties props = new Properties();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (is == null) {
                throw new IOException("Fichier de configuration introuvable : " + propertiesFile);
            }
            props.load(is);
        }

        // Port du serveur
        String portStr = props.getProperty("PORT_CONSULTATION");
        if (portStr == null) {
            throw new IOException("Clé PORT_CONSULTATION manquante dans " + propertiesFile);
        }
        this.portConsultation = Integer.parseInt(portStr.trim());

        // Taille du pool
        String poolStr = props.getProperty("POOL_SIZE", "10");
        this.poolSize = Integer.parseInt(poolStr.trim());

        //param bd
        this.dbUrl = props.getProperty("DB_URL");
        if (this.dbUrl == null) {
            throw new IOException("Clé DB_URL manquante dans " + propertiesFile);
        }

        this.dbUser = props.getProperty("DB_USER");
        if (this.dbUser == null) {
            throw new IOException("Clé DB_USER manquante dans " + propertiesFile);
        }

        this.dbPassword = props.getProperty("DB_PASSWORD");
        if (this.dbPassword == null) {
            throw new IOException("Clé DB_PASSWORD manquante dans " + propertiesFile);
        }
    }

    public int getPortConsultation() {
        return portConsultation;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}
