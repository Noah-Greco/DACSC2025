package common.protocol;

import server.generic.Reponse;

public class ReponseGetReports implements Reponse {
    // On ne transporte plus List<Report> en clair !
    private byte[] dataCryptes; // La liste sérialisée puis chiffrée
    private byte[] hmac;        // La signature pour vérifier que personne n'a touché

    private boolean success;
    private String message;

    // Constructeur Succès (Données chiffrées + HMAC)
    public ReponseGetReports(byte[] dataCryptes, byte[] hmac) {
        this.dataCryptes = dataCryptes;
        this.hmac = hmac;
        this.success = true;
        this.message = "OK";
    }

    // Constructeur Erreur (Pas de données)
    public ReponseGetReports(String message) {
        this.dataCryptes = null;
        this.hmac = null;
        this.success = false;
        this.message = message;
    }

    public byte[] getDataCryptes() { return dataCryptes; }
    public byte[] getHmac() { return hmac; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}