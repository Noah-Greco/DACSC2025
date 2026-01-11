package server.logic;

import common.crypto.MyCrypto;
import common.entity.Doctor;
import common.entity.Report;
import common.protocol.*;
import server.dao.DoctorDAO;
import server.dao.ReportDAO;
import server.generic.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MRPSProtocol implements Protocole {

    private Logger logger;
    private int connectedDoctorId = -1;
    private SecretKey sessionKey; // La clé AES partagée avec le client

    public MRPSProtocol(Logger logger) {
        this.logger = logger;
        // Important pour la crypto (Bouncy Castle)
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public String getNom() {
        return "MRPS v1.0";
    }

    @Override
    public synchronized Reponse TraiteRequete(Requete requete, Socket socket) throws FinConnexionException {

        // 1. Gestion du Login (et Handshake)
        if (requete instanceof RequeteLogin) {
            return TraiteRequeteLogin((RequeteLogin) requete);
        }

        // --- GESTION LOGOUT (Nouveau) ---
        if (requete instanceof RequeteLogout) {
            logger.Trace("Demande de LOGOUT reçue.");

            // On réinitialise l'état (C'est ce que tu as demandé)
            this.connectedDoctorId = -1;
            this.sessionKey = null; // On oublie aussi la clé de session par sécurité

            return new ReponseLogout(true, "Au revoir");
        }

        // --- SÉCURITÉ : Vérifier si connecté ---
        if (connectedDoctorId == -1) {
            return new ReponseGetReports("ERREUR LOGIN| Accès refusé : Non connecté.");
        }

        // 2. Gestion de la demande de rapports (Cryptée et Signée)
        if (requete instanceof RequeteGetReports) {
            return TraiteRequeteGetReports((RequeteGetReports) requete);
        }

        // 3. Gestion de l'ajout de rapport (ADD_REPORT)
        if (requete instanceof RequeteAddReport) {
            return TraiteRequeteAddReport((RequeteAddReport) requete);
        }

        // 4. Gestion de la modification (EDIT_REPORT)
        if (requete instanceof RequeteEditReport) {
            return TraiteRequeteEditReport((RequeteEditReport) requete);
        }

        return null;
    }

    private Reponse TraiteRequeteLogin(RequeteLogin req) throws FinConnexionException {
        logger.Trace("Tentative de login pour : " + req.getLogin());

        try {
            // 1. On interroge la BD via le DAO
            DoctorDAO dao = new DoctorDAO();
            Doctor doc = dao.getDoctorByLogin(req.getLogin());

            if (doc == null) {
                logger.Trace("Utilisateur inconnu en BD.");
                throw new FinConnexionException(new ReponseLogin(false, "Utilisateur inconnu"));
            }

            // 2. On récupère le mot de passe stocké (pour le calcul)
            String passwordEnDB = doc.getPassword();

            // 3. On lance la vérification cryptographique (Digest Salé)
            if (req.verifyPassword(passwordEnDB)) {
                this.connectedDoctorId = doc.getId();

                // --- HANDSHAKE SÉCURISÉ RSA ---
                logger.Trace("Login OK. Génération et chiffrement RSA de la clé de session...");

                try {
                    // 1. Générer Clé AES (Comme avant)
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
                    keyGen.init(128);
                    SecretKey secretKey = keyGen.generateKey();
                    this.sessionKey = secretKey; // On garde la version claire pour nous

                    // 2. Charger la Clé Publique du Client (depuis server.jks)
                    // (Dans un vrai projet, on choisirait l'alias selon le user, ici on hardcode "client")
                    java.security.PublicKey clientPublicKey = common.crypto.KeyStoreUtils.loadPublicKey(
                            "keystores/server.jks", "123456", "client"
                    );

                    // 3. Chiffrer la clé AES avec RSA
                    byte[] keyBytesAES = secretKey.getEncoded();
                    byte[] encryptedKey = MyCrypto.encryptRSA(clientPublicKey, keyBytesAES);

                    logger.Trace("Clé AES chiffrée avec RSA (" + encryptedKey.length + " bytes). Envoi.");

                    // 4. Envoyer le paquet chiffré
                    return new ReponseLogin(true, "Bienvenue Dr. " + doc.getLastName(), encryptedKey);

                } catch (Exception e) {
                    logger.Trace("Erreur Crypto Handshake : " + e.getMessage());
                    return new ReponseLogin(false, "Erreur Serveur (Crypto)");
                }
            } else {
                logger.Trace("Mauvais mot de passe.");
                throw new FinConnexionException(new ReponseLogin(false, "Mauvais mot de passe"));
            }

        } catch (FinConnexionException fce) {
            throw fce;
        } catch (Exception e) {
            logger.Trace("Erreur interne : " + e.getMessage());
            return new ReponseLogin(false, "Erreur serveur");
        }
    }

    private Reponse TraiteRequeteGetReports(RequeteGetReports req) {
        logger.Trace("Demande rapports. Médecin ID=" + this.connectedDoctorId + ", Filtre Patient=" + req.getPatientIdFilter());

        try {
            // 1. Récupération des données en clair depuis la BD
            ReportDAO dao = new ReportDAO();
            List<Report> reports;

            if (req.getPatientIdFilter() == -1) {
                reports = dao.getReportsByDoctor(this.connectedDoctorId);
            } else {
                reports = dao.getReportsByDoctorAndPatient(this.connectedDoctorId, req.getPatientIdFilter());
            }
            logger.Trace("Rapports trouvés en BD : " + reports.size());

            // 2. SÉRIALISATION (List -> byte[])
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(reports);
            byte[] dataClairs = baos.toByteArray();

            // --- TRACE DEBUG AVANT CRYPTAGE ---
            logger.Trace("[SERVEUR] Données CLAIRES (taille: " + dataClairs.length + " bytes) : " + Arrays.toString(Arrays.copyOfRange(dataClairs, 0, Math.min(dataClairs.length, 50))) + "...");

            // 3. CHIFFREMENT (AES) avec la clé de session stockée
            byte[] dataCryptes = MyCrypto.encryptAES(this.sessionKey, dataClairs);

            // --- TRACE DEBUG APRÈS CRYPTAGE ---
            logger.Trace("[SERVEUR] Données CRYPTÉES (taille: " + dataCryptes.length + " bytes) : " + Arrays.toString(Arrays.copyOfRange(dataCryptes, 0, Math.min(dataCryptes.length, 50))) + "...");

            // 4. SIGNATURE (HMAC) pour l'intégrité
            byte[] hmac = MyCrypto.generateHMAC(this.sessionKey, dataCryptes);

            logger.Trace("[SERVEUR] Signature HMAC générée : " + Arrays.toString(hmac));
            logger.Trace("Envoi des données sécurisées (Cryptées + HMAC).");

            // On renvoie la réponse sécurisée
            return new ReponseGetReports(dataCryptes, hmac);

        } catch (Exception e) {
            logger.Trace("Erreur lors du traitement GetReports : " + e.getMessage());
            e.printStackTrace();
            return new ReponseGetReports("Erreur serveur interne (Crypto/BD)");
        }
    }

    private Reponse TraiteRequeteAddReport(RequeteAddReport req) {
        logger.Trace("Réception d'un nouveau rapport crypté...");
        try {
            // 1. Vérification Intégrité (HMAC)
            if (!MyCrypto.verifyHMAC(this.sessionKey, req.getDataCryptes(), req.getHmac())) {
                logger.Trace("ALERTE : Signature HMAC invalide pour l'ajout de rapport !");
                return new ReponseAddReport(false, "Signature invalide (Données corrompues)");
            }

            // 2. Déchiffrement (AES)
            byte[] dataClairs = MyCrypto.decryptAES(this.sessionKey, req.getDataCryptes());

            // 3. Désérialisation
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(dataClairs);
            java.io.ObjectOutputStream oosData = null; // Pas besoin ici
            java.io.ObjectInputStream oisData = new java.io.ObjectInputStream(bais);
            Report r = (Report) oisData.readObject();

            // 4. Sécurité : On force l'ID du médecin avec celui de la session
            // (Pour empêcher un médecin d'écrire au nom d'un autre)
            r.setDoctorId(this.connectedDoctorId);

            logger.Trace("Rapport décrypté : Patient ID=" + r.getPatientId() + ", Contenu=" + r.getDescription());

            // 5. Insertion en BD
            ReportDAO dao = new ReportDAO();
            dao.insertReport(r);

            logger.Trace("Rapport inséré en base de données.");
            return new ReponseAddReport(true, "Rapport ajouté avec succès.");

        } catch (Exception e) {
            logger.Trace("Erreur ajout rapport : " + e.getMessage());
            e.printStackTrace();
            return new ReponseAddReport(false, "Erreur serveur");
        }
    }

    private Reponse TraiteRequeteEditReport(RequeteEditReport req) {
        logger.Trace("Réception d'une demande de modification cryptée...");
        try {
            // 1. Vérif HMAC
            if (!MyCrypto.verifyHMAC(this.sessionKey, req.getDataCryptes(), req.getHmac())) {
                return new ReponseEditReport(false, "Intégrité corrompue (HMAC invalide)");
            }

            // 2. Déchiffrement AES
            byte[] dataClairs = MyCrypto.decryptAES(this.sessionKey, req.getDataCryptes());

            // 3. Désérialisation
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(dataClairs);
            java.io.ObjectInputStream oisData = new java.io.ObjectInputStream(bais);
            Report r = (Report) oisData.readObject();

            // 4. Sécurité : On force l'ID du médecin pour s'assurer qu'il modifie bien SES rapports
            r.setDoctorId(this.connectedDoctorId);

            logger.Trace("Modification du rapport ID=" + r.getId());

            // 5. Update en BD
            ReportDAO dao = new ReportDAO();
            dao.updateReport(r);

            return new ReponseEditReport(true, "Rapport modifié avec succès.");

        } catch (Exception e) {
            logger.Trace("Erreur Edit : " + e.getMessage());
            return new ReponseEditReport(false, "Erreur : " + e.getMessage());
        }
    }
}