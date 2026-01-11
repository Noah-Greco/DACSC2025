package common.protocol;

import server.generic.Requete;

public class RequeteEditReport implements Requete {
    private byte[] dataCryptes; // Contient l'objet Report modifié (chiffré)
    private byte[] hmac;        // Signature intégrité

    public RequeteEditReport(byte[] dataCryptes, byte[] hmac) {
        this.dataCryptes = dataCryptes;
        this.hmac = hmac;
    }

    public byte[] getDataCryptes() { return dataCryptes; }
    public byte[] getHmac() { return hmac; }
}