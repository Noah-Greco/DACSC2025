package common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigLoader { //Utiliser dans Network, retournera l'ip et le port principalement

    private static final String CONFIG_FILE = "config.properties"; // Chemin à la racine des ressources
    private static final Properties PROPERTIES = new Properties(); //stock paire clé vlaleur

    static { //Execute automatiquement 1 seul fois lors de la 1er utilisation de la classe
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {//cherche fichier dans classpath
            if (input == null) {
                System.err.println("ERREUR FATALE: Impossible de trouver " + CONFIG_FILE);
                throw new RuntimeException("Fichier de configuration manquant: " + CONFIG_FILE);
            } else {
                PROPERTIES.load(input); //lis le contenue du fichier et remplit PROPERTIES
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des propriétés : " + ex.getMessage());
        }
    }
    //Cherche une clé dans Proprties et retourne sa valeur
    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Propriété de configuration manquante: " + key);
        }
        return value;
    }
}