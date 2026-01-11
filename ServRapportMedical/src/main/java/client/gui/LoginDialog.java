package client.gui;

import client.network.NetworkManager;
import common.protocol.ReponseLogin;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    public interface LoginSuccessCallback {
        void onSuccess(ReponseLogin response, String login);
    }


    private final JTextField loginField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnCancel = new JButton("Quitter");

    private final LoginSuccessCallback onSuccess;

    public LoginDialog(JFrame owner, LoginSuccessCallback onSuccess) {
        super(owner, "Connexion", true);
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> doLogin());
        btnCancel.addActionListener(e -> System.exit(0));

        getRootPane().setDefaultButton(btnLogin);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Login :"));
        panel.add(loginField);

        panel.add(new JLabel("Password :"));
        panel.add(passwordField);

        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(btnCancel);
        panel.add(btnLogin);
        return panel;
    }

    private void setUiEnabled(boolean enabled) {
        loginField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
    }

    private void doLogin() {
        String login = loginField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (login.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Champs vides", "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setUiEnabled(false);

        new Thread(() -> {
            try {
                NetworkManager nm = NetworkManager.getInstance();
                nm.connect(); // si déjà connecté, NetworkManager devrait gérer / ignorer

                ReponseLogin reponse = nm.sendLogin(login, pass);

                SwingUtilities.invokeLater(() -> {
                    if (reponse.isValide()) {
                        // Succès -> on ferme la pop-up, on notifie l'UI principale
                        dispose();
                        onSuccess.onSuccess(reponse, login);
                    } else {
                        JOptionPane.showMessageDialog(this, reponse.getMessage(), "Login KO", JOptionPane.ERROR_MESSAGE);
                        setUiEnabled(true);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erreur réseau : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    setUiEnabled(true);
                });
            }
        }).start();
    }
}
