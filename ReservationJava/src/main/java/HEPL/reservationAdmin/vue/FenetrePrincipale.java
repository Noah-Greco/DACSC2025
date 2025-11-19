package HEPL.reservationAdmin.vue;

import javax.swing.*;
import java.io.InputStream;

public class FenetrePrincipale extends JFrame {

    JPanel panel = new JPanel();
    public FenetrePrincipale() {
        setTitle("Client Admin Java");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*JButton btnAfficheConnexion = new JButton("Afficher les user connectés");

        btnAfficheConnexion.addActionListener(e -> {
            DialogAfficheConnexion dialogAfficheConnexion = new DialogAfficheConnexion(this);
        });
        panel.add(btnAfficheConnexion);*/

        setContentPane(panel);
    }
}