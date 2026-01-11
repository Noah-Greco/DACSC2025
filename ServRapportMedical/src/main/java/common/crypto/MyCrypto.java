package common.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Boîte à outils cryptographique.
 * Basé sur l'exemple MyCrypto du PDF 5 (Page 24), adapté pour AES.
 */
public class MyCrypto {

    // On s'assure que Bouncy Castle est chargé dès qu'on utilise cette classe
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Chiffre des données avec une clé AES (Symétrique).
     * @param cle La clé de session générée lors du Handshake
     * @param data Les données en clair (tableau d'octets)
     * @return Les données chiffrées
     */
    public static byte[] encryptAES(SecretKey cle, byte[] data) throws Exception {
        // 1. On prépare le moteur de chiffrement (Cipher)
        // "AES" = l'algo, "ECB" = le mode par bloc, "PKCS5Padding" = le remplissage
        // "BC" = on force l'utilisation de Bouncy Castle
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");

        // 2. On configure le moteur en mode CRYPTAGE (ENCRYPT_MODE) avec la clé
        cipher.init(Cipher.ENCRYPT_MODE, cle);

        // 3. On exécute le chiffrement
        return cipher.doFinal(data);
    }

    /**
     * Déchiffre des données avec une clé AES (Symétrique).
     * @param cle La MÊME clé de session
     * @param data Les données chiffrées
     * @return Les données en clair
     */
    public static byte[] decryptAES(SecretKey cle, byte[] data) throws Exception {
        // 1. On reprend exactement la même config
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");

        // 2. On configure le moteur en mode DÉCRYPTAGE (DECRYPT_MODE)
        cipher.init(Cipher.DECRYPT_MODE, cle);

        // 3. On exécute le déchiffrement
        return cipher.doFinal(data);
    }

    /**
            * Génère un HMAC (Signature avec clé secrète) pour garantir l'intégrité.
            * Algorithme : HMAC-SHA256 (Plus robuste que MD5)
     */
    public static byte[] generateHMAC(SecretKey key, byte[] data) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256", "BC");
        mac.init(key);
        return mac.doFinal(data);
    }

    /**
     * Vérifie si le HMAC reçu correspond aux données.
     */
    public static boolean verifyHMAC(SecretKey key, byte[] data, byte[] receivedHmac) throws Exception {
        byte[] calculatedHmac = generateHMAC(key, data);
        return java.security.MessageDigest.isEqual(calculatedHmac, receivedHmac);
    }

    /**
     * Chiffrement Asymétrique (RSA) - Utilisé pour le Handshake.
     * Le serveur utilise la Clé Publique du client pour chiffrer la clé de session.
     */
    public static byte[] encryptRSA(java.security.PublicKey key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * Déchiffrement Asymétrique (RSA) - Utilisé par le client.
     * Le client utilise sa Clé Privée pour récupérer la clé de session.
     */
    public static byte[] decryptRSA(java.security.PrivateKey key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}