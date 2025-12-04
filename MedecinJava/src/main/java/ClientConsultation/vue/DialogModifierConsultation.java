package ClientConsultation.vue;

import ClientConsultation.protocole.ServiceCAPClient;
import HEPL.medecinJava.model.entity.Consultation;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DialogModifierConsultation extends JDialog {

    private final Consultation consultation;
    private final ServiceCAPClient serviceCAP = new ServiceCAPClient();

    private JTextField txtDate;
    private JTextField txtHeure;

    private JButton btnValider;
    private JButton btnAnnuler;

    private boolean modified = false;

    public DialogModifierConsultation(Frame parent, Consultation consultation) {
        super(parent, "Modifier consultation " + consultation.getId(), true);
        this.consultation = consultation;

        setSize(400, 200);
        setLocationRelativeTo(parent);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel centre = new JPanel(new GridLayout(2, 2, 5, 5));

        centre.add(new JLabel("Date (yyyy-MM-dd) :"));
        txtDate = new JTextField(20);
        // pré-remplir avec la valeur actuelle
        txtDate.setText(consultation.getDateConsultation().toString());
        centre.add(txtDate);

        centre.add(new JLabel("Heure (HH:mm) :"));
        txtHeure = new JTextField(20);
        txtHeure.setText(consultation.getTimeConsultation().toString());
        centre.add(txtHeure);

        panel.add(centre, BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnValider = new JButton("Valider");
        btnAnnuler = new JButton("Annuler");
        bas.add(btnValider);
        bas.add(btnAnnuler);

        panel.add(bas, BorderLayout.SOUTH);

        setContentPane(panel);

        btnValider.addActionListener(e -> appliquerModification());
        btnAnnuler.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(btnValider);
    }

    private void appliquerModification() {
        String dateStr = txtDate.getText().trim();
        String heureStr = txtHeure.getText().trim();

        if (dateStr.isEmpty() || heureStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Date et heure sont obligatoires.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate nouvelleDate = LocalDate.parse(dateStr);
            LocalTime nouvelleHeure = LocalTime.parse(heureStr);

            // Ici, la consultation n'a PAS de patient (on l'a filtré dans FenetreConnecte)
            Integer idPatient = consultation.getPatient_id();  // peut rester null
            String raison = consultation.getReason();          // peut rester null

            serviceCAP.modifierConsultation(
                    consultation.getId(),
                    nouvelleDate,
                    nouvelleHeure,
                    idPatient,   // IMPORTANT : type Integer, peut être null
                    raison
            );

            modified = true;

            JOptionPane.showMessageDialog(this,
                    "Consultation modifiée.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formats attendus :\n- Date : yyyy-MM-dd\n- Heure : HH:mm",
                    "Erreur de format",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la modification : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    public boolean isModified() {
        return modified;
    }
}
