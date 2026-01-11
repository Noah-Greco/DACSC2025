package client.gui;

import client.network.NetworkManager;
import common.entity.Report;
import common.protocol.ReponseAddReport;
import common.protocol.ReponseEditReport;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportEditorDialog extends JDialog {

    private JTextField tfPatientId;
    private JTextField tfDate;
    private JTextArea taDescription;
    private JButton btnSave;
    private JButton btnCancel;

    private Report reportToEdit; // Null si c'est un ajout, rempli si c'est une édition
    private Runnable onSuccessCallback; // Fonction à appeler si la sauvegarde réussit (pour rafraîchir la liste)

    public ReportEditorDialog(Frame parent, Report report, Runnable onSuccess) {
        super(parent, report == null ? "Nouveau Rapport" : "Modifier Rapport #" + report.getId(), true);
        this.reportToEdit = report;
        this.onSuccessCallback = onSuccess;

        initGUI();

        // Si on est en mode édition, on pré-remplit
        if (report != null) {
            prefillData();
        }
    }

    private void initGUI() {
        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Champs ---

        // ID Patient
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("ID Patient :"), gbc);
        tfPatientId = new JTextField(20);
        gbc.gridx = 1; add(tfPatientId, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Date (yyyy-MM-dd) :"), gbc);
        tfDate = new JTextField(20);
        // Par défaut : Date du jour
        tfDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        gbc.gridx = 1; add(tfDate, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST; add(new JLabel("Contenu :"), gbc);
        taDescription = new JTextArea(8, 20);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        gbc.gridx = 1; add(new JScrollPane(taDescription), gbc);

        // --- Boutons ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton(reportToEdit == null ? "Ajouter" : "Modifier");
        btnCancel = new JButton("Annuler");

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(btnPanel, gbc);

        // --- Listeners ---
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());
    }

    private void prefillData() {
        // Mode Édition : on remplit les champs
        tfPatientId.setText(String.valueOf(reportToEdit.getPatientId()));
        // On empêche souvent de changer le patient d'un rapport existant (optionnel)
        tfPatientId.setEditable(false);

        tfDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(reportToEdit.getDate()));
        taDescription.setText(reportToEdit.getDescription());
    }

    private void handleSave() {
        try {
            // 1. Validation locale
            int patientId = Integer.parseInt(tfPatientId.getText().trim());
            String dateStr = tfDate.getText().trim();
            String description = taDescription.getText().trim();

            // Format SQL standard yyyy-MM-dd pour éviter les soucis
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La description ne peut pas être vide.");
                return;
            }

            btnSave.setEnabled(false); // Anti-double clic

            // 2. Appel Réseau (Thread séparé)
            new Thread(() -> {
                try {
                    NetworkManager nm = NetworkManager.getInstance();
                    boolean success = false;
                    String message = "";

                    if (reportToEdit == null) {
                        // --- CAS AJOUT ---
                        ReponseAddReport rep = nm.sendAddReport(patientId, description, date);
                        success = rep.isSuccess();
                        message = rep.getMessage();
                    } else {
                        // --- CAS ÉDITION ---
                        ReponseEditReport rep = nm.sendEditReport(reportToEdit.getId(), patientId, description, date);
                        success = rep.isSuccess();
                        message = rep.getMessage();
                    }

                    final boolean finalSuccess = success;
                    final String finalMessage = message;

                    // 3. Retour UI
                    SwingUtilities.invokeLater(() -> {
                        if (finalSuccess) {
                            JOptionPane.showMessageDialog(this, finalMessage, "Succès", JOptionPane.INFORMATION_MESSAGE);
                            dispose(); // Fermer la fenêtre
                            if (onSuccessCallback != null) onSuccessCallback.run(); // Rafraîchir la liste parente
                        } else {
                            JOptionPane.showMessageDialog(this, "Erreur : " + finalMessage, "Erreur Serveur", JOptionPane.ERROR_MESSAGE);
                            btnSave.setEnabled(true);
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Erreur réseau : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        btnSave.setEnabled(true);
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "L'ID Patient doit être un nombre.", "Erreur format", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format de date invalide (yyyy-MM-dd attendu).", "Erreur format", JOptionPane.WARNING_MESSAGE);
        }
    }
}