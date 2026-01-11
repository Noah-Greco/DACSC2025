package common.entity;


public class Doctor implements Entity {

    private int id;
    private int specialtyId;
    private String lastName;
    private String firstName;

    private String username;
    private String password;

    public Doctor(int id, int specialtyId, String lastName, String firstName, String username, String password) {
        this.id = id;
        this.specialtyId = specialtyId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.username = username;
        this.password = password;
    }

    @Override
    public int getId() {
        return id;
    }

    public  void setId(int id) {}

    public int getSpecialtyId() {
        return specialtyId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }



    @Override
    public String toString() {
        return "Dr. " + firstName + " " + lastName + " (Login: " + username + ")";
    }
}