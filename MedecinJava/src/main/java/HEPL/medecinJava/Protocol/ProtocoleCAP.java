package HEPL.medecinJava.Protocol;

import HEPL.medecinJava.config.ConfigConsultation;

// DAOs
import HEPL.medecinJava.model.dao.*;

// Entities / VM – À ADAPTER aux vrais noms de tes classes
import HEPL.medecinJava.model.entity.*;
import HEPL.medecinJava.model.viewmodel.*;

import ServeurGeneriqueTCP.*;

import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProtocoleCAP implements Protocole {

    private final ConfigConsultation config;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;
    private final ConsultationDAO consultationDAO;

    public ProtocoleCAP(ConfigConsultation config) {
        this.config = config;

        // Les DAO se débrouillent avec ConnectionBD pour lire le fichier properties
        this.doctorDAO = new DoctorDAO();
        this.patientDAO = new PatientDAO();
        this.consultationDAO = new ConsultationDAO();
    }


    @Override
    public String getNom() {
        return "CAP";
    }

    @Override
    public Reponse TraiteRequete(Requete requete, Socket socket) throws FinConnexionException {

        if (requete instanceof RequeteCAPLogin r) {
            return traiteLogin(r);
        }
        if (requete instanceof RequeteCAPAddConsultation r) {
            return traiteAddConsultation(r);
        }
        if (requete instanceof RequeteCAPAddPatient r) {
            return traiteAddPatient(r);
        }
        if (requete instanceof RequeteCAPUpdateConsultation r) {
            return traiteUpdateConsultation(r);
        }
        if (requete instanceof RequeteCAPSearchConsultations r) {
            return traiteSearchConsultations(r);
        }
        if (requete instanceof RequeteCAPDeleteConsultation r) {
            return traiteDeleteConsultation(r);
        }
        if (requete instanceof RequeteCAPLogout r) {
            return traiteLogout(r);
        }

        throw new IllegalArgumentException("Type de requête CAP inconnu : " + requete.getClass());
    }

    // ============================================================
    //                    MÉTHODES PRIVÉES
    // ============================================================

    // ------------ LOGIN ------------
    private ReponseCAPLogin traiteLogin(RequeteCAPLogin r) {
        try {
            Doctor doc = doctorDAO.findByIdAndPassword(r.getDoctorId(), r.getPassword());

            if (doc == null) {
                return new ReponseCAPLogin(false, "ID ou mot de passe incorrect");
            }

            String msg = "Bienvenue " + doc.getLastName() + " " + doc.getFirstName();
            return new ReponseCAPLogin(true, msg);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ReponseCAPLogin(false, "Erreur BD lors du login : " + e.getMessage());
        }
    }


    // ------------ ADD_CONSULTATION ------------
    private ReponseCAPAddConsultation traiteAddConsultation(RequeteCAPAddConsultation r) {
        try {
            // Il manque dans notre requête un id du médecin.
            // Normalement, la BD doit savoir pour quel médecin on crée
            // les consultations. Donc tu dois AJOUTER un champ idMedecin
            // dans RequeteCAPAddConsultation et le récupérer ici.
            //
            // int idMedecin = r.getIdMedecin(); // TODO après modif de la classe requête

            int idMedecin = 0; // TODO: À corriger dès que tu ajoutes l'id dans la requête

            // ====================================================
            // ICI : appel à ConsultationDAO pour créer les consultations.
            // Exemple POSSIBLE (À ADAPTER) :
            //
            // boolean ok = consultationDAO.createConsultations(
            //                  idMedecin,
            //                  r.getDate(),
            //                  r.getHeure(),
            //                  r.getDureeMinutes(),
            //                  r.getNbConsultationsConsecutives());
            // ====================================================

            // TODO: remplace par la vraie méthode de ton DAO
            boolean ok = consultationDAO.addConsultations(
                    idMedecin,
                    r.getDate(),
                    r.getHeure(),
                    r.getDureeMinutes(),
                    r.getNbConsultationsConsecutives()
            );

            if (!ok) {
                return new ReponseCAPAddConsultation(false,
                        "Impossible de créer les consultations (dépasser 17h00 ou conflit horaire)");
            }
            return new ReponseCAPAddConsultation(true, "Consultations créées");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ReponseCAPAddConsultation(false,
                    "Erreur BD lors de la création : " + e.getMessage());
        }
    }

    // ------------ ADD_PATIENT ------------
    private ReponseCAPAddPatient traiteAddPatient(RequeteCAPAddPatient r) {
        try {
            // ====================================================
            // ICI : appel à PatientDAO pour ajouter un patient.
            // Exemple POSSIBLE :
            //    int id = patientDAO.insert(r.getNom(), r.getPrenom());
            // ====================================================

            // TODO: remplace par la bonne méthode
            int id = patientDAO.addPatient(r.getNom(), r.getPrenom());

            return new ReponseCAPAddPatient(true, id, "Patient ajouté");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ReponseCAPAddPatient(false, -1,
                    "Erreur BD lors de l'ajout du patient : " + e.getMessage());
        }
    }

    // ------------ UPDATE_CONSULTATION ------------
    private ReponseCAPUpdateConsultation traiteUpdateConsultation(RequeteCAPUpdateConsultation r) {
        try {
            // ====================================================
            // ICI : appel à ConsultationDAO pour mettre à jour la consultation.
            // Exemple POSSIBLE :
            //
            // boolean ok = consultationDAO.updateConsultation(
            //                  r.getIdConsultation(),
            //                  r.getNouvelleDate(),
            //                  r.getNouvelleHeure(),
            //                  r.getIdPatient(),
            //                  r.getRaison());
            // ====================================================

            // TODO: remplace par la vraie méthode
            boolean ok = consultationDAO.updateConsultation(
                    r.getIdConsultation(),
                    r.getNouvelleDate(),
                    r.getNouvelleHeure(),
                    r.getIdPatient(),
                    r.getRaison()
            );

            if (!ok)
                return new ReponseCAPUpdateConsultation(false, "Mise à jour impossible");

            return new ReponseCAPUpdateConsultation(true, "Consultation mise à jour");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ReponseCAPUpdateConsultation(false,
                    "Erreur BD lors de la mise à jour : " + e.getMessage());
        }
    }

    // ------------ SEARCH_CONSULTATIONS ------------
    private ReponseCAPSearchConsultations traiteSearchConsultations(RequeteCAPSearchConsultations r) {
        try {
            // 1. Construire le SearchVM à partir de la requête CAP
            ConsultationSearchVM vm = new ConsultationSearchVM();

            // Si ta requête CAP a bien ces getters (à adapter si noms différents)
            if (r.getIdPatient() != null) {
                vm.setPatientId(r.getIdPatient());
            }
            if (r.getDate() != null) {
                vm.setDateConsultation(r.getDate());
                vm.setDateConsultationTo(r.getDate()); // même jour, si c'est la logique voulue
            }

            // 2. Lancer la recherche via le DAO
            // load(vm) renvoie déjà un ArrayList<Consultation>
            var consultations = consultationDAO.load(vm);

            // 3. Message
            String msg = "Nombre de consultations trouvées : " +
                    (consultations == null ? 0 : consultations.size());

            // 4. Réponse CAP (on renvoie directement les entités Consultation)
            return new ReponseCAPSearchConsultations(consultations, msg);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ReponseCAPSearchConsultations(
                    null,
                    "Erreur BD lors de la recherche : " + e.getMessage()
            );
        }
    }



    // ------------ DELETE_CONSULTATION ------------
    private ReponseCAPDeleteConsultation traiteDeleteConsultation(RequeteCAPDeleteConsultation r) {
        try {
            // ====================================================
            // ICI : appel à ConsultationDAO pour supprimer.
            // Exemple POSSIBLE :
            //    boolean ok = consultationDAO.delete(r.getIdConsultation());
            // ====================================================

            // TODO: remplace par la vraie méthode
            boolean ok = consultationDAO.deleteConsultation(r.getIdConsultation());

            if (!ok)
                return new ReponseCAPDeleteConsultation(false, "Suppression impossible");

            return new ReponseCAPDeleteConsultation(true, "Consultation supprimée");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ReponseCAPDeleteConsultation(false,
                    "Erreur BD lors de la suppression : " + e.getMessage());
        }
    }

    // ------------ LOGOUT ------------
    private ReponseCAPLogout traiteLogout(RequeteCAPLogout r) {
        // Pour un serveur de requêtes, LOGOUT sera souvent juste informatif.
        return new ReponseCAPLogout("Logout OK pour " + r.getLogin());
    }
}
