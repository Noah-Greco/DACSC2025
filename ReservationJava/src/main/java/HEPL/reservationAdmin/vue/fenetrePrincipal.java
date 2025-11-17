package HEPL.Restaurant.Vue;

import javax.swing.*;
import java.io.InputStream;

public class FenetrePrincipale extends JFrame {

    private final String utilisateurCourant;
    public FenetrePrincipale() {
        this.utilisateurCourant = utilisateurCourant;

        setTitle("Client Admin Java");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();

        // 1. Bouton “Visualiser Menu”
        JButton btnVisualiserMenu = new JButton("Visualiser Menu");
        btnVisualiserMenu.addActionListener(e -> {
            DialogVisualiserMenu dialogMenu = new DialogVisualiserMenu(this);
            dialogMenu.setVisible(true);
        });
        panel.add(btnVisualiserMenu);

        // 2. Bouton “Nouvelle commande”
        JButton btnCommande = new JButton("Nouvelle commande");
        btnCommande.addActionListener(e -> {
            DialogNouvelleCommande dialogCommande = new DialogNouvelleCommande(this, utilisateurCourant);
            dialogCommande.setVisible(true);
        });
        panel.add(btnCommande);

        // 3. Bouton “Tables”
        JButton btnTables = new JButton("Tables");
        btnTables.addActionListener(e -> {
            DialogGestionTables dialogTables = new DialogGestionTables(this, utilisateurCourant);
            dialogTables.setVisible(true);
        });
        panel.add(btnTables);

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Vérification de l’existence du fichier users.properties
            InputStream in = FenetrePrincipale.class.getResourceAsStream("/users.properties");
            if (in == null) {
                System.err.println("users.properties introuvable !");
            } else {
                System.out.println("users.properties chargé avec succès.");
            }

            // 2. Création d’un frame factice pour héberger le dialog de connexion
            JFrame dummyFrame = new JFrame();
            dummyFrame.setUndecorated(true);
            dummyFrame.setSize(0, 0);
            dummyFrame.setLocationRelativeTo(null);
            dummyFrame.setVisible(true);

            // 3. Affichage du dialog de connexion (avec option “S’inscrire”)
            DialogConnexionUtilisateur loginDlg = new DialogConnexionUtilisateur(dummyFrame);
            loginDlg.setVisible(true);

            if (loginDlg.estConnecte()) {
                // 4. Récupérer le login
                String utilisateur = loginDlg.getNomUtilisateur();

                // 5. Fermer le dummyFrame et ouvrir la fenêtre principale en passant le login
                dummyFrame.dispose();
                FenetrePrincipale mainWindow = new FenetrePrincipale(utilisateur);
                mainWindow.setVisible(true);
            } else {
                // 6. En cas d'échec ou annulation, quitter
                System.exit(0);
            }
        });
    }
}