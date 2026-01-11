package common.protocol;

import server.generic.Requete;

public class RequeteAddReport implements Requete {
    private byte[] dataCryptes; // Le rapport sérialisé et chiffré (AES)
    private byte[] hmac;        // La signature d'intégrité

    public RequeteAddReport(byte[] dataCryptes, byte[] hmac) {
        this.dataCryptes = dataCryptes;
        this.hmac = hmac;
    }

    public byte[] getDataCryptes() { return dataCryptes; }
    public byte[] getHmac() { return hmac; }
}