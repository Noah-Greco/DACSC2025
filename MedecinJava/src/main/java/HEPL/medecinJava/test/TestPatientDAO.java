package HEPL.medecinJava.test;

import HEPL.medecinJava.model.dao.PatientDAO;
import HEPL.medecinJava.model.entity.Patient;
import HEPL.medecinJava.model.viewmodel.PatientSearchVM;

import java.time.LocalDate;
import java.util.ArrayList;

public class TestPatientDAO {

    public static void main(String[] args) {

        System.out.println("=== TEST PATIENT DAO ===");

        PatientDAO dao = new PatientDAO();

        // TEST 1 : CHARGER TOUS LES PATIENTS
        System.out.println("\nTEST 1 : load()");

        ArrayList<Patient> all = dao.load();
        System.out.println("Patients trouvés : " + all.size());
        for (Patient p : all) {
            System.out.println(p);
        }

        // TEST 2 : RECHERCHE PAR NOM (LIKE)
        System.out.println("\nTEST 2 : load(PatientSearchVM)");

        PatientSearchVM vm = new PatientSearchVM();
        vm.setLastName("a");
        ArrayList<Patient> filtrés = dao.load(vm);

        System.out.println("Patients avec nom commençant par 'a' : " + filtrés.size());
        for (Patient p : filtrés) {
            System.out.println(p);
        }

        // TEST 3 : INSERT
        System.out.println("\nTEST 3 : save() (INSERT)");

        Patient newP = new Patient("TestFirst", "TestLast", LocalDate.of(1990, 1, 1));
        dao.save(newP); // INSERT
        System.out.println("NOUVEAU patient ID = " + newP.getId());

        // TEST 4 : UPDATE
        System.out.println("\nTEST 4 : save() (UPDATE)");

        newP.setLastName("TestLastUpdated");
        dao.save(newP); // UPDATE

        PatientSearchVM vm2 = new PatientSearchVM();
        vm2.setId(newP.getId());
        ArrayList<Patient> updated = dao.load(vm2);

        System.out.println("Patient mis à jour : ");
        for (Patient p : updated) {
            System.out.println(p);
        }

        // TEST 5 : DELETE
        System.out.println("\nTEST 5 : delete()");

        dao.delete(newP);
        ArrayList<Patient> afterDelete = dao.load(vm2);

        System.out.println("Patients trouvés après suppression : " + afterDelete.size());
    }
}
