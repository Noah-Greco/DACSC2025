package HEPL.medecinJava.test;

import HEPL.medecinJava.model.dao.SpecialtyDAO;
import HEPL.medecinJava.model.entity.Specialty;
import HEPL.medecinJava.model.viewmodel.SpecialtySearchVM;

import java.util.ArrayList;

public class TestSpecialtyDAO {

    public static void main(String[] args) {

        System.out.println("=== TEST SPECIALTY DAO ===");

        SpecialtyDAO dao = new SpecialtyDAO();

        // TEST 1 : load()
        System.out.println("\nTEST 1 : load()");
        ArrayList<Specialty> all = dao.load();
        System.out.println("Specialties trouvées : " + all.size());
        for (Specialty s : all) {
            System.out.println(s);
        }

        // TEST 2 : load(SpecialtySearchVM) - filtre par nom
        System.out.println("\nTEST 2 : load(SpecialtySearchVM) - nom commence par 'C'");
        SpecialtySearchVM vm = new SpecialtySearchVM();
        vm.setName("C");
        ArrayList<Specialty> filtres = dao.load(vm);

        System.out.println("Specialties avec nom commençant par 'C' : " + filtres.size());
        for (Specialty s : filtres) {
            System.out.println(s);
        }

        // TEST 3 : INSERT
        System.out.println("\nTEST 3 : save() (INSERT)");
        Specialty newS = new Specialty("TestSpecialty");
        dao.save(newS);
        System.out.println("Nouvelle spécialité ID = " + newS.getId());

        // TEST 4 : UPDATE
        System.out.println("\nTEST 4 : save() (UPDATE)");
        newS.setName("UpdatedSpecialty");
        dao.save(newS);

        SpecialtySearchVM vm2 = new SpecialtySearchVM();
        vm2.setId(newS.getId());
        ArrayList<Specialty> updated = dao.load(vm2);
        System.out.println("Spécialité mise à jour : ");
        for (Specialty s : updated) {
            System.out.println(s);
        }

        // TEST 5 : DELETE
        System.out.println("\nTEST 5 : delete()");
        dao.delete(newS);
        ArrayList<Specialty> afterDelete = dao.load(vm2);
        System.out.println("Spécialités trouvées après suppression : " + afterDelete.size());
    }
}
