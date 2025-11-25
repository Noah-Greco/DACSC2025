package HEPL.medecinJava.test;

import HEPL.medecinJava.model.dao.ConsultationDAO;
import HEPL.medecinJava.model.entity.Consultation;
import HEPL.medecinJava.model.viewmodel.ConsultationSearchVM;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class TestConsultationDAO {

    public static void main(String[] args) {

        System.out.println("=== TEST CONSULTATION DAO ===");

        ConsultationDAO dao = new ConsultationDAO();

        // TEST 1 : load()
        System.out.println("\nTEST 1 : load()");
        ArrayList<Consultation> all = dao.load();
        System.out.println("Consultations trouvées : " + all.size());
        for (Consultation c : all) {
            System.out.println(c);
        }

        // TEST 2 : load(ConsultationSearchVM) - filtre par médecin
        System.out.println("\nTEST 2 : load(ConsultationSearchVM) - doctor_id = 4");

        ConsultationSearchVM vm = new ConsultationSearchVM();
        vm.setDoctorId(4);

        ArrayList<Consultation> filtres = dao.load(vm);
        System.out.println("Consultations pour doctor_id=4 : " + filtres.size());
        for (Consultation c : filtres) {
            System.out.println(c);
        }

        // TEST 3 : INSERT
        System.out.println("\nTEST 3 : save() (INSERT)");

        Consultation newC = new Consultation(
                -1,
                1,          // doctor_id
                1,          // patient_id
                LocalDate.of(2025, 12, 1),
                LocalTime.of(15, 30),
                "Test Consultation"
        );

        dao.save(newC);
        System.out.println("Nouvelle consultation ID = " + newC.getId());

        // TEST 4 : UPDATE
        System.out.println("\nTEST 4 : save() (UPDATE)");

        newC.setReason("Updated Consultation");
        dao.save(newC);

        ConsultationSearchVM vm2 = new ConsultationSearchVM();
        vm2.setId(newC.getId());

        ArrayList<Consultation> updated = dao.load(vm2);
        System.out.println("Consultation mise à jour : ");
        for (Consultation c : updated) {
            System.out.println(c);
        }

        // TEST 5 : DELETE
        System.out.println("\nTEST 5 : delete()");

        dao.delete(newC);

        ArrayList<Consultation> afterDelete = dao.load(vm2);
        System.out.println("Consultations trouvées après suppression : " + afterDelete.size());
    }
}
