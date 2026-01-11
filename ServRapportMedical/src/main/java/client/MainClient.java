package client;

import client.gui.RapportMedicalClientUI;

import javax.swing.*;

public class MainClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RapportMedicalClientUI::new);
    }
}
