package common.crypto;

import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

public class KeyStoreUtils {

    // Charge une Clé Privée depuis un Keystore JKS
    public static PrivateKey loadPrivateKey(String keystorePath, String password, String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        // On charge depuis les ressources (classpath)
        try (InputStream is = KeyStoreUtils.class.getClassLoader().getResourceAsStream(keystorePath)) {
            if (is == null) throw new RuntimeException("Keystore introuvable : " + keystorePath);
            ks.load(is, password.toCharArray());
        }
        return (PrivateKey) ks.getKey(alias, password.toCharArray());
    }

    // Charge une Clé Publique (depuis un certificat dans le Keystore)
    public static PublicKey loadPublicKey(String keystorePath, String password, String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream is = KeyStoreUtils.class.getClassLoader().getResourceAsStream(keystorePath)) {
            if (is == null) throw new RuntimeException("Keystore introuvable : " + keystorePath);
            ks.load(is, password.toCharArray());
        }
        Certificate cert = ks.getCertificate(alias);
        return cert.getPublicKey();
    }
}