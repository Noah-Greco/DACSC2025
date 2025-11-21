package HEPL.reservationAdmin.vue;

import javax.swing.*;
import java.io.InputStream;

public class FenetrePrincipale extends JFrame {

    private String[] allClients;
    JPanel panel = new JPanel();
    public FenetrePrincipale(String[] clients) {

        this.allClients = clients;

        setTitle("Client Admin Java");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton btnAfficheConnexion = new JButton("Afficher les user connectés");

        btnAfficheConnexion.addActionListener(e -> {
            DialogAfficheConnexion dialogAfficheConnexion = new DialogAfficheConnexion(this, this.allClients);
            dialogAfficheConnexion.setVisible(true);
        });
        panel.add(btnAfficheConnexion);

        setContentPane(panel);
    }
}