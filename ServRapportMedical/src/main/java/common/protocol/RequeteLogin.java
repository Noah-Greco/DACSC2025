package common.protocol;

import server.generic.Requete;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RequeteLogin implements Requete {
    private String login;
    private long temps;
    private double alea;
    private byte[] digest; // Le résumé cryptographique envoyé

    // Appelée par le CLIENT
    public RequeteLogin(String login, String password) throws Exception {
        this.login = login;
        // 1. Création du sel (Ingrédients uniques)
        this.temps = new Date().getTime();
        this.alea = Math.random();

        // TRACE DEBUG CLIENT
        System.out.println("\n--- [CLIENT] CRÉATION DU DIGEST ---");
        System.out.println("[CLIENT] Mot de passe clair saisi : " + password);
        System.out.println("[CLIENT] Sel utilisé (Temps)      : " + temps);
        System.out.println("[CLIENT] Sel utilisé (Aléa)       : " + alea);

        // 2. Initialisation du Hachage (SHA-1 via Bouncy Castle)
        // Assure-toi que BouncyCastle est chargé dans le main !
        MessageDigest md = MessageDigest.getInstance("SHA-1", "BC");

        // 3. Ajout des ingrédients (Login + Password + Sel)
        md.update(login.getBytes());
        md.update(password.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(temps);
        dos.writeDouble(alea);

        md.update(baos.toByteArray()); // On ajoute le sel au mélange

        this.digest = md.digest(); // On ferme l'enveloppe

        // TRACE RÉSULTAT
        System.out.println("[CLIENT] Digest calculé (Hash)    : " + Arrays.toString(this.digest));
        System.out.println("-----------------------------------\n");
    }

    // Appelée par le SERVEUR
    public boolean verifyPassword(String passwordLocal) throws Exception {
        System.out.println("\n--- [SERVEUR] VÉRIFICATION CRYPTO ---");
        System.out.println("[SERVEUR] Mot de passe venant de la BD : " + passwordLocal);
        System.out.println("[SERVEUR] Digest reçu du client        : " + Arrays.toString(this.digest));

        // Le serveur refait la même recette avec le mot de passe qu'il connait (DB)
        MessageDigest md = MessageDigest.getInstance("SHA-1", "BC");
        md.update(login.getBytes());
        md.update(passwordLocal.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(temps); // Il utilise le sel reçu du client !
        dos.writeDouble(alea);

        md.update(baos.toByteArray());

        byte[] digestLocal = md.digest();
        System.out.println("[SERVEUR] Digest recalculé localement  : " + Arrays.toString(digestLocal));

        boolean match = MessageDigest.isEqual(this.digest, digestLocal);
        System.out.println("[SERVEUR] Comparaison (Match ?)        : " + (match ? "OUI (OK)" : "NON (Echec)"));
        System.out.println("--------------------------------------\n");

        return match;
    }

    public String getLogin() { return login; }
}