package ClientConsultation.vue;

import HEPL.medecinJava.model.viewmodel.ConsultationSearchVM;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DialogNouvelleConsultation extends JDialog {

    private JPanel panel = new JPanel();

    private JTextField txtDate;// yyyy-MM-dd
    private JTextField txtTimeFrom; // HH:mm
    private JTextField txtTimeTo;// HH:mm

    private JButton btnValider;
    private JButton btnAnnuler;

    private boolean validated = false;
    private ConsultationSearchVM consultationVM;

    private int idMedecin;

    public DialogNouvelleConsultation(Frame parent, int idMedecin) {
        super(parent, "Nouvelle consultation", true); // modal

        this.idMedecin = idMedecin;

        setSize(400, 220);
        setLocationRelativeTo(parent);

        initComponents();
    }

    private void initComponents() {
        panel.setLayout(new BorderLayout(10, 10));

        // Centre : champs date + heuraires
        JPanel centre = new JPanel(new GridLayout(3, 2, 5, 5));

        centre.add(new JLabel("Date (yyyy-MM-dd) :"));
        txtDate = new JTextField(20);
        centre.add(txtDate);

        centre.add(new JLabel("Heure début (HH:mm) :"));
        txtTimeFrom = new JTextField(20);
        centre.add(txtTimeFrom);

        centre.add(new JLabel("Heure fin (HH:mm) :"));
        txtTimeTo = new JTextField(20);
        centre.add(txtTimeTo);

        panel.add(centre, BorderLayout.CENTER);

        // Bas : boutons
        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnValider = new JButton("Valider");
        btnAnnuler = new JButton("Annuler");
        bas.add(btnValider);
        bas.add(btnAnnuler);

        panel.add(bas, BorderLayout.SOUTH);

        setContentPane(panel);

        btnValider.addActionListener(e -> validerConsultation());
        btnAnnuler.addActionListener(e -> {
            validated = false;
            dispose();
        });

        getRootPane().setDefaultButton(btnValider);
    }

    private void validerConsultation() {
        String dateStr = txtDate.getText().trim();
        String timeFromStr = txtTimeFrom.getText().trim();
        String timeToStr = txtTimeTo.getText().trim();

        if (dateStr.isEmpty() || timeFromStr.isEmpty() || timeToStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Date, heure début et heure fin sont obligatoires.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime timeFrom = LocalTime.parse(timeFromStr);
            LocalTime timeTo = LocalTime.parse(timeToStr);

            if (!timeTo.isAfter(timeFrom)) {
                JOptionPane.showMessageDialog(this,
                        "L'heure de fin doit être après l'heure de début.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ConsultationSearchVM vm = new ConsultationSearchVM();
            vm.setDoctorId(idMedecin);
            vm.setDateConsultation(date);
            vm.setTimeConsultation(timeFrom);
            vm.setTimeConsultationTo(timeTo);

            this.consultationVM = vm;
            this.validated = true;

            dispose();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formats attendus :\n- Date : yyyy-MM-dd\n- Heure : HH:mm",
                    "Erreur de format",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isValidated() {
        return validated;
    }

    public ConsultationSearchVM getConsultationVM() {
        return consultationVM;
    }
}
