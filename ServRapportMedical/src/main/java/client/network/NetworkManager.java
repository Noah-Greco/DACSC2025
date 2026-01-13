package client.network;

import common.config.ConfigLoader;
import common.crypto.MyCrypto;
import common.entity.Report;
import common.protocol.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.Socket;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private SecretKey sessionKey; // La clé AES stockée ici
//La classe sert a ouvrir et maintenir une connexion TCP serveur
    private NetworkManager() {
        Security.addProvider(new BouncyCastleProvider());
    }

    //Logique Singleton
    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void connect() throws IOException {
        if (socket == null || socket.isClosed()) {
            String host = ConfigLoader.getProperty("SERVER_IP");//recup l'ip apd du fichier config.proporties
            int port = Integer.parseInt(ConfigLoader.getProperty("PORT_REPORT_SECURE"));

            System.out.println("Connexion vers " + host + ":" + port);

            socket = new Socket(host, port); //création connexion tcp
            oos = new ObjectOutputStream(socket.getOutputStream());//envoie donnée
            ois = new ObjectInputStream(socket.getInputStream());//recois donnée
        }
    }

    public ReponseLogin sendLogin(String login, String password) throws Exception {
        RequeteLogin req = new RequeteLogin(login, password);
        oos.writeObject(req);//envoie requete login au serveur

        Object rep = ois.readObject();
        if (rep instanceof ReponseLogin) {
            ReponseLogin reponse = (ReponseLogin) rep; //vérifie que c bien la rep login

            if (reponse.isValide() && reponse.getSessionKey() != null) {
                System.out.println("[CLIENT] Handshake : Clé chiffrée reçue. Déchiffrement RSA...");

                try {
                    // 1. Charger la Clé Privée du client ( moi )
                    java.security.PrivateKey myPrivateKey = common.crypto.KeyStoreUtils.loadPrivateKey(
                            "keystores/client.jks", "123456", "client"
                    );

                    // 2. Déchiffrer la clé de session (utilise RSA --> pour avoir AES)
                    byte[] encryptedKey = reponse.getSessionKey();
                    byte[] aesKeyBytes = MyCrypto.decryptRSA(myPrivateKey, encryptedKey);//utilise ma clé privé pour déchiffrer

                    // 3. Reconstruire la clé AES
                    this.sessionKey = new SecretKeySpec(aesKeyBytes, "AES");

                    System.out.println("[CLIENT] Clé AES déchiffrée et stockée avec succès.");

                } catch (Exception e) {
                    System.err.println("[CLIENT] Erreur déchiffrement RSA : " + e.getMessage());
                    throw new Exception("Échec du Handshake sécurisé");
                }
            }
            return reponse;
        }
        throw new Exception("Réponse inconnue au login");
    }

    public SecretKey getSessionKey() { return sessionKey; }

    // Envoi de la demande et Réception sécurisée
    public List<Report> sendGetReports(int patientIdFilter) throws Exception {
        // 1. Envoi de la requête
        oos.writeObject(new RequeteGetReports(patientIdFilter));

        // 2. Réception de la réponse
        Object rep = ois.readObject();

        if (rep instanceof ReponseGetReports) {
            ReponseGetReports response = (ReponseGetReports) rep;

            if (!response.isSuccess()) {
                throw new Exception(response.getMessage());
            }

            // --- TRACE DEBUG RÉCEPTION ---
            System.out.println("\n--- [CLIENT] RÉCEPTION DES RAPPORTS ---");
            byte[] recu = response.getDataCryptes();
            System.out.println("[CLIENT] Données reçues (CRYPTÉES) : " + Arrays.toString(Arrays.copyOfRange(recu, 0, Math.min(recu.length, 50))) + "...");
            System.out.println("[CLIENT] HMAC reçu : " + Arrays.toString(response.getHmac()));

            // A.Vérification HMAC
            //-->Le serveur a calculé un HMAC sur dataCryptes avec la clé de session.
            //-->Le client recalcule le HMAC localement avec sessionKey.
            //-->Si les deux ne matchent pas : arrêt
            if (!MyCrypto.verifyHMAC(this.sessionKey, response.getDataCryptes(), response.getHmac())) {
                throw new Exception("ALERTE SÉCURITÉ : HMAC invalide !");
            }
            System.out.println("[CLIENT] Vérification HMAC : OK (Intégrité confirmée)");

            // B. Déchiffrement AES avec la sessionkey
            byte[] dataClairs = MyCrypto.decryptAES(this.sessionKey, response.getDataCryptes());

            // --- TRACE
            System.out.println("[CLIENT] Données déchiffrées (CLAIRES) : " + Arrays.toString(Arrays.copyOfRange(dataClairs, 0, Math.min(dataClairs.length, 50))) + "...");

            // C. Désérialisation : byte --> objet java
            ByteArrayInputStream bais = new ByteArrayInputStream(dataClairs);
            ObjectInputStream oisData = new ObjectInputStream(bais);

            @SuppressWarnings("unchecked") //Convertit un objet sérialisé en List<Report>
            List<Report> liste = (List<Report>) oisData.readObject();

            System.out.println("[CLIENT] Liste reconstruite : " + liste.size() + " rapports récupérés.");
            System.out.println("---------------------------------------\n");

            return liste;
        }
        throw new Exception("Réponse inconnue");
    }

    public ReponseAddReport sendAddReport(int patientId, String description, java.util.Date date) throws Exception {
        // 1. Création de l'objet métier (ID tmp 0, DoctorID sera mis par le serveur)
        // Note: On ne connait pas notre  ID médecin ici, le serveur le sait via la session
        Report rapport = new Report(0, 0, patientId, date, description);

        // 2. Sérialisation (Objet -> byte[])
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oosData = new ObjectOutputStream(baos);
        oosData.writeObject(rapport);
        byte[] dataClairs = baos.toByteArray();

        // 3. Chiffrement (AES) avec la clé de session
        byte[] dataCryptes = MyCrypto.encryptAES(this.sessionKey, dataClairs);

        // 4. Signature (HMAC)
        byte[] hmac = MyCrypto.generateHMAC(this.sessionKey, dataCryptes);

        // 5. Envoi
        oos.writeObject(new RequeteAddReport(dataCryptes, hmac));

        // 6. Réception confirmation
        Object rep = ois.readObject();
        if (rep instanceof ReponseAddReport) {
            return (ReponseAddReport) rep;
        }
        throw new Exception("Réponse inconnue lors de l'ajout du rapport");
    }
    public ReponseEditReport sendEditReport(int reportId, int patientId, String description, java.util.Date date) throws Exception {
        // 1. Création de l'objet métier avec l'ID du rapport à modifier
        common.entity.Report rapport = new common.entity.Report(reportId, 0, patientId, date, description);

        // 2. Sérialisation
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oosData = new ObjectOutputStream(baos);
        oosData.writeObject(rapport);
        byte[] dataClairs = baos.toByteArray();

        // 3. Chiffrement (AES)
        byte[] dataCryptes = MyCrypto.encryptAES(this.sessionKey, dataClairs);

        // 4. Signature (HMAC)
        byte[] hmac = MyCrypto.generateHMAC(this.sessionKey, dataCryptes);

        // 5. Envoi
        oos.writeObject(new RequeteEditReport(dataCryptes, hmac));

        // 6. Réception
        Object rep = ois.readObject();
        if (rep instanceof ReponseEditReport) {
            return (ReponseEditReport) rep;
        }
        throw new Exception("Réponse inconnue lors de la modification");
    }

    public void sendLogout() {
        try {
            // 1. On envoie le signal
            oos.writeObject(new RequeteLogout());

            // 2. On attend juste la confirmation (pour être synchro)
            ois.readObject();

            // 3. On nettoie la clé locale
            this.sessionKey = null;
            System.out.println("[CLIENT] Logout effectué. Clé session effacée.");

        } catch (Exception e) {
            System.err.println("Erreur lors du logout : " + e.getMessage());
            // Pas grave si ça plante ici, on se déconnecte quand même
        }
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (oos != null) oos.close();
            if (ois != null) ois.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}