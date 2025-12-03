package ClientConsultation.vue;

import javax.swing.*;
import java.io.InputStream;

public class FenetrePrincipale extends JFrame {

    JPanel panel = new JPanel();
    public FenetrePrincipale() {
        setTitle("Client Consultation java");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton btnLogin = new JButton("Se Connecter");
        panel.add(btnLogin);

        setContentPane(panel);

        btnLogin.addActionListener(e -> ouvrirDialogLogin());
    }

    private void ouvrirDialogLogin() {
        DialogLogin dialogLogin = new DialogLogin(this);
        dialogLogin.setVisible(true);   // BLOQUANT tant que le dialog n'est pas fermé

        if (dialogLogin.isAuthenticated()) {
            int idMedecin = dialogLogin.getIdMedecin();
            System.out.println(idMedecin);


            FenetreConnecte fenetreconnecte = new FenetreConnecte(idMedecin);
            fenetreconnecte.setVisible(true);

            dispose();
        }
    }
}