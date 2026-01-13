package client.gui;

import client.network.NetworkManager;
import common.entity.Report;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RapportMedicalClientUI extends JFrame {
    private JTextField patientIdFilterField;
    private JButton searchButton;
    private DefaultTableModel tableModel;
    private JTable reportsTable;
    private JButton addButton, editButton;
    private JButton logoutButton;
    private JPanel topPanel;
    private JPanel filterPanel;
    private JPanel displayPanel;
    private JPanel actionPanel;
    private JLabel helloLabel;
    
    public RapportMedicalClientUI() {
        super("Dossier Médical Sécurisé (MRPS) - Dr. Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        topPanel = createTopPanel();
        filterPanel = createFilterPanel();

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(filterPanel, BorderLayout.CENTER);

        displayPanel = createReportDisplayPanel();

        actionPanel = createActionButtonsPanel();

        add(northContainer, BorderLayout.NORTH);
        add(displayPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        setSize(1100, 700);
        setLocationRelativeTo(null);

        setEtatConnecte(false);
        setVisible(true);

        // Ouvre la pop-up au lancement
        SwingUtilities.invokeLater(this::showLoginPopup);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        helloLabel = new JLabel("Bonjour");
        helloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        helloLabel.setFont(new Font("SansSherif", Font.BOLD, 14));

        logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoutButton.addActionListener(e -> handleLogout());

        panel.add(helloLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(logoutButton);

        return panel;
    }
    private void showLoginPopup() {
        // Pop-up modale: on ne peut pas utiliser l'appli tant que pas connecté
        LoginDialog dialog = new LoginDialog(this, (response, login) -> {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Login OK", JOptionPane.INFORMATION_MESSAGE);
            helloLabel.setText("Bonjour Dr. " + login);
            setEtatConnecte(true);
        });
        dialog.setVisible(true);

    }
    private void handleLogout() {
        new Thread(() -> { //Créer thread réseau pour déco
            try {
                NetworkManager.getInstance().sendLogout();

            } catch (Exception ex) {
            }

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Au revoir !", "Déconnexion", JOptionPane.INFORMATION_MESSAGE);
                setEtatConnecte(false);
                showLoginPopup();
            });
        }).start();
    }
    private void setEtatConnecte(boolean connecte) { //Boolean permetant d'afficher l'app complet si connecter et inversement si pas connecter
        setPanelEnabled(filterPanel, connecte);
        setPanelEnabled(displayPanel, connecte);
        setPanelEnabled(actionPanel, connecte);

        logoutButton.setEnabled(connecte);
        helloLabel.setVisible(connecte);

        if (!connecte) {
            patientIdFilterField.setText("");
            tableModel.setRowCount(0);
            helloLabel.setText("Bonjour");
        }
    }
    //Desactive le panel et tt ses enfants pour aps avoir de beug visuel
    private void setPanelEnabled(JPanel panel, boolean isEnabled) {
        panel.setEnabled(isEnabled);
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) setPanelEnabled((JPanel) c, isEnabled);
            else c.setEnabled(isEnabled);
        }
        if (reportsTable != null) reportsTable.setEnabled(isEnabled);
    }
    private JPanel createFilterPanel() { //panneau de filtre
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Filtrer les Rapports :"));

        patientIdFilterField = new JTextField(10);
        searchButton = new JButton("Lister les rapports");

        JPanel pInternal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pInternal.add(new JLabel("ID Patient (Vide=Tous) :"));
        pInternal.add(patientIdFilterField);
        pInternal.add(searchButton);
        panel.add(pInternal);

        searchButton.addActionListener(e -> handleGetReports());

        return panel;
    }

    private JPanel createReportDisplayPanel() { //Tableau rapport
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Rapports trouvés :"));

        String[] columns = {"ID", "Date", "Patient ID", "Médecin ID", "Extrait"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        reportsTable = new JTable(tableModel);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        reportsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Double-clic
                if (e.getClickCount() == 2 && reportsTable.isEnabled()) {
                    int row = reportsTable.getSelectedRow();
                    if (row == -1) return;

                    // Récupérer les données de la ligne
                    String id = (String) tableModel.getValueAt(row, 0);
                    String date = (String) tableModel.getValueAt(row, 1);
                    String patientId = (String) tableModel.getValueAt(row, 2);
                    String doctorId = (String) tableModel.getValueAt(row, 3);
                    String desc = (String) tableModel.getValueAt(row, 4);

                    // Construire un texte lisible
                    String texte =
                            "Rapport ID : " + id + "\n"
                                    + "Date : " + date + "\n"
                                    + "Patient ID : " + patientId + "\n"
                                    + "Médecin ID : " + doctorId + "\n\n"
                                    + "Contenu :\n" + desc;

                    // Ouvrir la pop-up
                    openReadReportDialog(texte);
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(reportsTable);
        panel.add(scrollTable, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionButtonsPanel() { //Ajout et modifie les rapports
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Encoder Nouveau Rapport");
        editButton = new JButton("Modifier Rapport Sélectionné");

        panel.add(addButton);
        panel.add(editButton);

        addButton.addActionListener(e -> new ReportEditorDialog(this, null, this::handleGetReports).setVisible(true));

        editButton.addActionListener(e -> {
            int selectedRow = reportsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un rapport dans la liste.", "Info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                //recup les infos d'un rapport et en fait un objet Rapport
                int id = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
                String dateStr = (String) tableModel.getValueAt(selectedRow, 1);
                int patientId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 2));
                int doctorId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 3));
                String desc = (String) tableModel.getValueAt(selectedRow, 4);

                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);

                Report selectedReport = new Report(id, doctorId, patientId, date, desc);
                new ReportEditorDialog(this, selectedReport, this::handleGetReports).setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lecture ligne : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        return panel;
    }
    // RÉSEAU
    //Appeler qd on veut lister les rapports, appelle le serveur pour récup la liste des rapports et remplit swing avec
    private void handleGetReports() {
        searchButton.setEnabled(false); //empeche double clic

        String filterText = patientIdFilterField.getText().trim();
        int filterId = -1;
        try {
            if (!filterText.isEmpty()) filterId = Integer.parseInt(filterText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Patient invalide", "Erreur", JOptionPane.WARNING_MESSAGE);
            searchButton.setEnabled(true);
            return;
        }
        //derniere version filtrer du filtre
        final int fid = filterId;
        new Thread(() -> {
            try {
                //On envoie la requete au serveur et attends sa réponse contenenant la liste des rapports
                List<Report> rapports = NetworkManager.getInstance().sendGetReports(fid);

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    if (rapports.isEmpty()) {
                    } else {
                        //affiche dans l'ui tt les rapports
                        for (Report r : rapports) {
                            tableModel.addRow(new Object[]{
                                    String.valueOf(r.getId()),
                                    r.getDate().toString(),
                                    String.valueOf(r.getPatientId()),
                                    String.valueOf(r.getDoctorId()),
                                    r.getDescription()
                            });
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
                });
            } finally {
                SwingUtilities.invokeLater(() -> searchButton.setEnabled(true));
            }
        }).start();
    }
    // POPUP permettant de lire le rapport en txt
    private void openReadReportDialog(String text) {
        JDialog dialog = new JDialog(this, "Lecture Rapport", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setMargin(new Insets(10, 10, 10, 10));

        dialog.add(new JScrollPane(area));

        JButton btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel p = new JPanel();
        p.add(btnClose);
        dialog.add(p, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
