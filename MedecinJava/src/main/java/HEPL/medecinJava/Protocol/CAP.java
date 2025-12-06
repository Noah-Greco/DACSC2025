package HEPL.medecinJava.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CAP implements Closeable {

    //Classes internes : requête / réponse

    private static class CAPRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        private String command;
        private HashMap<String, Object> params = new HashMap<>();

        public CAPRequest(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public void put(String key, Object value) {
            params.put(key, value);
        }

        public Object get(String key) {
            return params.get(key);
        }

        public HashMap<String, Object> getParams() {
            return params;
        }
    }

    private static class CAPResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private boolean ok;
        private String message;
        private Object data;

        public CAPResponse(boolean ok, String message, Object data) {
            this.ok = ok;
            this.message = message;
            this.data = data;
        }

        public boolean isOk() {
            return ok;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }

    //Partie "client protocole"

    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    //Ouvre la connexion vers le serveur de consultations.
    public CAP(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        connect();
    }

    private void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(5000); // 5 secondes timeout lecture

        // IMPORTANT : créer d'abord l'ObjectOutputStream puis l'ObjectInputStream
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.oos.flush();
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    //Envoie une requête CAP et reçoit la réponse.
    private synchronized CAPResponse sendRequest(CAPRequest req)
            throws IOException, ClassNotFoundException {

        oos.writeObject(req);
        oos.flush();

        Object rep = ois.readObject();
        if (!(rep instanceof CAPResponse)) {
            throw new IOException("Réponse invalide (type inattendu) : " + rep);
        }
        return (CAPResponse) rep;
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            try {
                logout(); // par sécurité
            } catch (Exception ignored) {}
            socket.close();
        }
    }

    // ==========================================================
    //               COMMANDES DU PROTOCOLE CAP
    // ==========================================================

    // ---------- LOGIN ----------
    public boolean login(String login, String password) throws IOException {
        CAPRequest req = new CAPRequest("LOGIN");
        req.put("login", login);
        req.put("password", password);

        try {
            CAPResponse rep = sendRequest(req);
            return rep.isOk();
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (LOGIN)", e);
        }
    }

    // ---------- ADD_CONSULTATION ----------
    public boolean addConsultation(String date,
                                   String heure,
                                   int dureeMinutes,
                                   int nbConsecutives) throws IOException {
        CAPRequest req = new CAPRequest("ADD_CONSULTATION");
        req.put("date", date);
        req.put("heure", heure);
        req.put("duree", dureeMinutes);
        req.put("nbConsecutives", nbConsecutives);

        try {
            CAPResponse rep = sendRequest(req);
            return rep.isOk();
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (ADD_CONSULTATION)", e);
        }
    }

    // ---------- ADD_PATIENT ----------
    public int addPatient(String nom, String prenom) throws IOException {
        CAPRequest req = new CAPRequest("ADD_PATIENT");
        req.put("nom", nom);
        req.put("prenom", prenom);

        try {
            CAPResponse rep = sendRequest(req);
            if (!rep.isOk()) return -1;

            Object data = rep.getData();
            if (data instanceof Integer) {
                return (Integer) data;
            }
            if (data instanceof String) {
                try {
                    return Integer.parseInt((String) data);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
            return -1;
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (ADD_PATIENT)", e);
        }
    }

    // ---------- UPDATE_CONSULTATION ----------
    public boolean updateConsultation(int idConsultation,
                                      String nouvelleDate,
                                      String nouvelleHeure,
                                      Integer patientId,
                                      String raison) throws IOException {
        CAPRequest req = new CAPRequest("UPDATE_CONSULTATION");
        req.put("id", idConsultation);
        req.put("date", nouvelleDate);
        req.put("heure", nouvelleHeure);
        req.put("patientId", patientId);
        req.put("raison", raison);

        try {
            CAPResponse rep = sendRequest(req);
            return rep.isOk();
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (UPDATE_CONSULTATION)", e);
        }
    }

    // ---------- SEARCH_CONSULTATIONS ----------
    @SuppressWarnings("unchecked")
    public List<Object> searchConsultations(Integer patientId, String date) throws IOException {
        CAPRequest req = new CAPRequest("SEARCH_CONSULTATIONS");
        req.put("patientId", patientId);
        req.put("date", date);

        try {
            CAPResponse rep = sendRequest(req);
            if (!rep.isOk()) return new ArrayList<>();

            Object data = rep.getData();
            if (data instanceof List<?>) {
                return (List<Object>) data; // tu recasteras en List<ConsultationVM> plus tard
            }
            // rien ou format inattendu
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (SEARCH_CONSULTATIONS)", e);
        }
    }

    // ---------- DELETE_CONSULTATION ----------
    public boolean deleteConsultation(int idConsultation) throws IOException {
        CAPRequest req = new CAPRequest("DELETE_CONSULTATION");
        req.put("id", idConsultation);

        try {
            CAPResponse rep = sendRequest(req);
            return rep.isOk();
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (DELETE_CONSULTATION)", e);
        }
    }

    // ---------- LOGOUT ----------
    public void logout() throws IOException {
        CAPRequest req = new CAPRequest("LOGOUT");

        try {
            sendRequest(req); // on ignore la réponse
        } catch (ClassNotFoundException e) {
            throw new IOException("Réponse illisible (LOGOUT)", e);
        }
    }
}
