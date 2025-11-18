package hepl.reservationAdmin.classe;

import hepl.reservationAdmin.vue.FenetrePrincipale;

import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        Socket clientSocket;
        clientSocket = new Socket("127.0.0.1", 8090);



        FenetrePrincipale fen = new FenetrePrincipale();
        fen.setVisible(true);
    }
}