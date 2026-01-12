package common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigLoader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("ERREUR FATALE: Impossible de trouver " + CONFIG_FILE);
                throw new RuntimeException("Fichier de configuration manquant: " + CONFIG_FILE);
            } else {
                PROPERTIES.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des propriétés : " + ex.getMessage());
        }
    }

    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Propriété de configuration manquante: " + key);
        }
        return value;
    }
}