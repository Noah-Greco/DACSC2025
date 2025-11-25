package HEPL.medecinJava.model.viewmodel;

public class PatientSearchVM {

    // null = pas de filtre
    //Je ne compte pas faire de recherche sur la birth date
    // c pour ça que pas de champ annif
    private Integer id;
    private String firstName;
    private String lastName;

    public PatientSearchVM() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
