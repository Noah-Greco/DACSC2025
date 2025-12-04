package ClientConsultation.protocole;

import HEPL.medecinJava.Protocol.*;
import HEPL.medecinJava.model.viewmodel.ConsultationSearchVM;
import HEPL.medecinJava.model.entity.Consultation;
import ServeurGeneriqueTCP.Requete;
import ServeurGeneriqueTCP.Reponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import HEPL.medecinJava.Protocol.RequeteCAPAddConsultation;
import HEPL.medecinJava.Protocol.ReponseCAPAddConsultation;
import HEPL.medecinJava.model.viewmodel.ConsultationSearchVM;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class ServiceCAPClient {

    private final String host = "127.0.0.1"; // à adapter si besoin
    private final int port = 50000;          // à adapter si besoin

    private Reponse envoyer(Requete req) throws Exception {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            oos.writeObject(req);
            oos.flush();

            return (Reponse) ois.readObject();
        }
    }

    public boolean login(int doctorId, String password) throws Exception {
        RequeteCAPLogin req = new RequeteCAPLogin(doctorId, password);

        ReponseCAPLogin rep = (ReponseCAPLogin) envoyer(req);

        return rep.isOk();
    }

    @SuppressWarnings("unchecked")
    public List<Consultation> getConsultationsMedecin(Integer doctorId) throws Exception {
        RequeteCAPSearchConsultations req =
                new RequeteCAPSearchConsultations(doctorId, null);

        ReponseCAPSearchConsultations rep =
                (ReponseCAPSearchConsultations) envoyer(req);

        // Le serveur renvoie une List<Consultation>, donc on caste en conséquence
        return (List<Consultation>) rep.getConsultations();
    }

    public boolean supprimerConsultation(Integer idConsultation) throws Exception
    {
        RequeteCAPDeleteConsultation req =  new RequeteCAPDeleteConsultation(idConsultation);

        ReponseCAPDeleteConsultation rep = (ReponseCAPDeleteConsultation) envoyer(req);

        return rep.isOk();
    }

    public void ajouterConsultation(ConsultationSearchVM vm) throws Exception {

        Integer idMedecin = vm.getDoctorId();
        LocalDate date = vm.getDateConsultation();
        LocalTime timeFrom = vm.getTimeConsultation();
        LocalTime timeTo = vm.getTimeConsultationTo();

        if (idMedecin == null || date == null || timeFrom == null || timeTo == null) {
            throw new Exception("Données de consultation incomplètes.");
        }

        long dureeLong = Duration.between(timeFrom, timeTo).toMinutes();
        if (dureeLong <= 0) {
            throw new Exception("Heure de fin invalide.");
        }

        int dureeMinutes = (int) dureeLong;
        int nbConsultationsConsecutives = 1; // une seule consultation

        RequeteCAPAddConsultation req =
                new RequeteCAPAddConsultation(
                        idMedecin,
                        date,
                        timeFrom,
                        dureeMinutes,
                        nbConsultationsConsecutives
                );

        ReponseCAPAddConsultation rep =
                (ReponseCAPAddConsultation) envoyer(req);

        if (!rep.isOk()) {  // adapte si ton getter s’appelle différemment
            throw new Exception(rep.getMessage());
        }
    }

    public void modifierConsultation(int idConsultation,
                                     LocalDate nouvelleDate,
                                     LocalTime nouvelleHeure,
                                     Integer idPatient,
                                     String raison) throws Exception {

        RequeteCAPUpdateConsultation req =
                new RequeteCAPUpdateConsultation(
                        idConsultation,
                        nouvelleDate,
                        nouvelleHeure,
                        idPatient,     // peut être null
                        raison         // peut être null
                );

        ReponseCAPUpdateConsultation rep =
                (ReponseCAPUpdateConsultation) envoyer(req);

        if (!rep.isOk()) {  // adapte le getter si nécessaire
            throw new Exception(rep.getMessage());
        }
    }


    public int ajouterPatient(String nom, String prenom) throws Exception {
        RequeteCAPAddPatient req = new RequeteCAPAddPatient(nom, prenom);

        Object repObj = envoyer(req);
        if (repObj == null) {
            throw new Exception("Réponse serveur nulle (addPatient).");
        }

        ReponseCAPAddPatient rep = (ReponseCAPAddPatient) repObj;

        if (!rep.isOk()) {
            String msg = rep.getMessage();
            if (msg == null || msg.isEmpty()) {
                msg = "Erreur inconnue côté serveur (message null).";
            }
            throw new Exception(msg);
        }

        return rep.getIdPatient();
    }


}
