package HEPL.medecinJava.test;

import HEPL.medecinJava.model.dao.DoctorDAO;
import HEPL.medecinJava.model.entity.Doctor;
import HEPL.medecinJava.model.viewmodel.DoctorSearchVM;

import java.util.ArrayList;

public class TestDoctorDAO {

    public static void main(String[] args) {

        System.out.println("=== TEST DOCTOR DAO ===");

        DoctorDAO dao = new DoctorDAO();

        // TEST 1 : load()
        System.out.println("\nTEST 1 : load()");
        ArrayList<Doctor> all = dao.load();
        System.out.println("Doctors trouvés : " + all.size());
        for (Doctor d : all) {
            System.out.println(d);
        }

        // TEST 2 : load(DoctorSearchVM) - filtre par spécialité
        System.out.println("\nTEST 2 : load(DoctorSearchVM) - spécialité 1");
        DoctorSearchVM vm = new DoctorSearchVM();
        vm.setSpecialtyId(1);   // adapte selon tes données
        ArrayList<Doctor> filtres = dao.load(vm);
        System.out.println("Doctors avec specialty_id = 1 : " + filtres.size());
        for (Doctor d : filtres) {
            System.out.println(d);
        }

        // TEST 3 : INSERT
        System.out.println("\nTEST 3 : save() (INSERT)");
        Doctor newD = new Doctor(1, "TestLast", "TestFirst");
        dao.save(newD);
        System.out.println("Nouveau doctor ID = " + newD.getId());

        // TEST 4 : UPDATE
        System.out.println("\nTEST 4 : save() (UPDATE)");
        newD.setLastName("UpdatedLast");
        dao.save(newD);
        DoctorSearchVM vm2 = new DoctorSearchVM();
        vm2.setId(newD.getId());
        ArrayList<Doctor> updated = dao.load(vm2);
        System.out.println("Doctor mis à jour : ");
        for (Doctor d : updated) {
            System.out.println(d);
        }

        // TEST 5 : DELETE
        System.out.println("\nTEST 5 : delete()");
        dao.delete(newD);
        ArrayList<Doctor> afterDelete = dao.load(vm2);
        System.out.println("Doctors trouvés après suppression : " + afterDelete.size());
    }
}
