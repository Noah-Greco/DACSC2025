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

}
