package ClientConsultation.vue;

import ClientConsultation.protocole.ServiceCAPClient;

import javax.swing.*;
import java.awt.*;

public class DialogNouveauPatient extends JDialog {

    private JTextField txtNom;
    private JTextField txtPrenom;
    private JButton btnValider;
    private JButton btnAnnuler;

    private final ServiceCAPClient serviceCAP = new ServiceCAPClient();

    private boolean created = false;
    private int idPatientCree;

    public DialogNouveauPatient(Frame parent) {
        super(parent, "Nouveau patient", true);

        setSize(400, 200);
        setLocationRelativeTo(parent);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel centre = new JPanel(new GridLayout(2, 2, 5, 5));
        centre.add(new JLabel("Nom :"));
        txtNom = new JTextField(20);
        centre.add(txtNom);

        centre.add(new JLabel("Prénom :"));
        txtPrenom = new JTextField(20);
        centre.add(txtPrenom);

        panel.add(centre, BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnValider = new JButton("Valider");
        btnAnnuler = new JButton("Annuler");
        bas.add(btnValider);
        bas.add(btnAnnuler);

        panel.add(bas, BorderLayout.SOUTH);

        setContentPane(panel);

        btnValider.addActionListener(e -> creerPatient());
        btnAnnuler.addActionListener(e -> {
            created = false;
            dispose();
        });

        getRootPane().setDefaultButton(btnValider);
    }

    private void creerPatient() {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nom et prénom sont obligatoires.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = serviceCAP.ajouterPatient(nom, prenom);

            this.idPatientCree = id;
            this.created = true;

            JOptionPane.showMessageDialog(this,
                    "Patient ajouté avec l'ID " + id,
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'ajout du patient : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    public boolean isCreated() {
        return created;
    }

    public int getIdPatientCree() {
        return idPatientCree;
    }
}
