package HEPL.reservationAdmin.vue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DialogAfficheConnexion extends JDialog {

    private JTable table;
    private DefaultTableModel model;

    public DialogAfficheConnexion(JFrame parent, String[] clients) {
        super(parent, "Clients connectés", true);
        initComponents(clients);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents(String[] clients) {

        // 1) Colonnes
        String[] colonnes = { "Adresse IP", "Nom", "Prénom", "ID" };

        // 2) Tableau de données (à générer depuis clients)
        String[][] data = convertirClients(clients);

        // 3) ICI → création du modèle
        model = new DefaultTableModel(data, colonnes) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 4) La JTable
        table = new JTable(model);

        // 5) Ajout dans un JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);

        // 6) Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private String[][] convertirClients(String[] clients) {

        // Compte seulement les lignes valides
        int count = 0;
        for (String c : clients) {
            if (c != null && !c.isBlank() && c.split(";").length == 4)
                count++;
        }

        String[][] array = new String[count][4];

        int i = 0;
        for (String c : clients) {
            if (c == null || c.isBlank()) continue;

            String[] parts = c.split(";");
            if (parts.length != 4) continue;

            array[i++] = parts;
        }

        return array;
    }
}
