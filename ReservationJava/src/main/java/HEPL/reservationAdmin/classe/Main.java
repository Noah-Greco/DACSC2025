package HEPL.reservationAdmin.classe;

import HEPL.reservationAdmin.vue.FenetrePrincipale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        Socket clientSocket;
        clientSocket = new Socket("192.168.2.129", 8090);

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out.print("ACBP#ALL_CLIENT##//##");
        out.flush();

        String reponse = LecteurProtocole.lireMessage(clientSocket.getInputStream());
        System.out.println("Réponse du serveur : " + reponse);

        String[] parts = reponse.split("#", 3);
        if (parts.length < 3) {
            System.out.println("Format incorrect : " + reponse);
            return;
        }

        String tag = parts[0];      // ALL_CLIENT
        String status = parts[1];   // ok / ko
        String data = parts[2];     // ip;nom;prenom;id#...

        if (!tag.equals("ALL_CLIENT")) {
            System.out.println("Message non attendu : " + tag);
            return;
        }

        if (!status.equals("ok")) {
            System.out.println("Erreur serveur : " + tag + "#" + status + "#" + data);
            return;
        }

        String[] clients = data.split("#");

        FenetrePrincipale fen = new FenetrePrincipale(clients);
        fen.setVisible(true);
    }
}