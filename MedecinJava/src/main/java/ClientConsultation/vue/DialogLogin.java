package ClientConsultation.vue;

import ClientConsultation.protocole.ServiceCAPClient;

import javax.swing.*;
import java.awt.*;

public class DialogLogin extends JDialog {

    private JPanel panel = new JPanel();
    private JTextField txtId;         // ID médecin
    private JPasswordField txtPassword;
    private JButton btnValider;
    private JButton btnAnnuler;

    private boolean authenticated = false;
    private int idMedecin;

    private final ServiceCAPClient serviceCAP = new ServiceCAPClient();

    public DialogLogin(Frame parent) {
        super(parent, "Connexion médecin", true); // modal

        setSize(400, 200);
        setLocationRelativeTo(parent);

        initComponents();
    }

    private void initComponents() {
        panel.setLayout(new BorderLayout(10, 10));

        JPanel centre = new JPanel(new GridLayout(2, 2, 5, 5));
        centre.add(new JLabel("ID Médecin :"));
        txtId = new JTextField(20);
        centre.add(txtId);

        centre.add(new JLabel("Mot de passe :"));
        txtPassword = new JPasswordField(20);
        centre.add(txtPassword);

        panel.add(centre, BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnValider = new JButton("Valider");
        btnAnnuler = new JButton("Annuler");
        bas.add(btnValider);
        bas.add(btnAnnuler);

        panel.add(bas, BorderLayout.SOUTH);

        setContentPane(panel);

        btnValider.addActionListener(e -> tenterConnexion());
        btnAnnuler.addActionListener(e -> {
            authenticated = false;
            dispose();
        });

        getRootPane().setDefaultButton(btnValider);
    }

    private void tenterConnexion() {
        String idStr = txtId.getText().trim();
        String pwd   = new String(txtPassword.getPassword());

        if (idStr.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ID et mot de passe obligatoires.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            boolean ok = serviceCAP.login(id, pwd);

            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "ID ou mot de passe incorrect.",
                        "Connexion échouée",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.idMedecin = id;
            this.authenticated = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "L'ID médecin doit être un nombre entier.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de communication avec le serveur : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public int getIdMedecin() {
        return idMedecin;
    }
}
