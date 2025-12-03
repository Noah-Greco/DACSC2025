package ClientConsultation.vue;

import ClientConsultation.protocole.ServiceCAPClient;
import HEPL.medecinJava.model.entity.Consultation;
import HEPL.medecinJava.model.viewmodel.ConsultationSearchVM;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FenetreConnecte extends JFrame {

    private Integer idMedecin;

    private JTable tableConsultations;
    private DefaultTableModel modelConsultations;

    private final ServiceCAPClient serviceCAP = new ServiceCAPClient();

    public FenetreConnecte(int idMedecin) {
        this.idMedecin = idMedecin;

        setTitle("Consultation Client - Médecin " + idMedecin);
        setSize(2000, 2000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        chargerConsultations();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnModifConsultation = new JButton("Modifier les consultations");
        JButton btnAjoutPatient = new JButton("Ajouter un patient");
        JButton btnAjoutConsultation = new JButton("Ajouter une consultation");
        JButton btnSuppConsultation = new JButton("Supprimer une consultation");
        JButton btnDeconnexion = new JButton("Déconnexion");

        panelBoutons.add(btnModifConsultation);
        panelBoutons.add(btnAjoutPatient);
        panelBoutons.add(btnAjoutConsultation);
        panelBoutons.add(btnSuppConsultation);
        panelBoutons.add(btnDeconnexion);

        panel.add(panelBoutons, BorderLayout.NORTH);

        // 5 colonnes cohérentes avec les données
        String[] colonnes = {"ID", "Date", "Heure", "Patient", "Motif"};
        modelConsultations = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableConsultations = new JTable(modelConsultations);
        JScrollPane scroll = new JScrollPane(tableConsultations);
        panel.add(scroll, BorderLayout.CENTER);

        setContentPane(panel);

        btnModifConsultation.addActionListener(e -> {
            DialogModifierConsultation modifierConsultation =
                    new DialogModifierConsultation(this);
            modifierConsultation.setVisible(true);
            // après modif : chargerConsultations();
        });

        btnAjoutPatient.addActionListener(e -> {
            DialogNouveauPatient nouveauPatient = new DialogNouveauPatient(this);
            nouveauPatient.setVisible(true);
            // après ajout : recharger si besoin
        });

        btnAjoutConsultation.addActionListener(e -> {
            DialogNouvelleConsultation nouvelleConsultation =
                    new DialogNouvelleConsultation(this);
            nouvelleConsultation.setVisible(true);
            // après ajout : recharger si besoin
        });

        btnSuppConsultation.addActionListener(e -> supprimerConsultation());

        btnDeconnexion.addActionListener(e -> {
            int rep = JOptionPane.showConfirmDialog(this,
                    "Se déconnecter ?",
                    "Déconnexion",
                    JOptionPane.YES_NO_OPTION);
            if (rep == JOptionPane.YES_OPTION) {
                dispose();
                FenetrePrincipale fen = new FenetrePrincipale();
                fen.setVisible(true);
            }
        });
    }

    private void chargerConsultations() {
        modelConsultations.setRowCount(0);

        try {
            List<Consultation> liste =
                    serviceCAP.getConsultationsMedecin(idMedecin);

            for (Consultation c : liste) {
                Object[] row = {
                        c.getId(),
                        c.getDateConsultation(),
                        c.getTimeConsultation(),
                        c.getPatient_id(),
                        c.getReason()
                };
                modelConsultations.addRow(row);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des consultations : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void supprimerConsultation() {
        int row = tableConsultations.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionne une consultation.");
            return;
        }
        Integer idConsultation = (Integer) modelConsultations.getValueAt(row, 0);

        int rep = JOptionPane.showConfirmDialog(this,
                "Supprimer la consultation " + idConsultation + " ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (rep != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean ok = serviceCAP.supprimerConsultation(idConsultation);

            if (ok) {
                chargerConsultations(); // rafraîchir le tableau
                JOptionPane.showMessageDialog(this,
                        "Consultation supprimée.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch (Exception ex) {
            // 1. afficher l'erreur à l'utilisateur
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);

            // 2. afficher dans la console pour le dev
            ex.printStackTrace();
        }

        chargerConsultations();
    }
}
