package common.entity;


public class Doctor implements Entity {

    private final int id;
    private final int specialtyId;
    private final String lastName;
    private final String firstName;


    public Doctor(int id, int specialtyId, String lastName, String firstName) {
        this.id = id;
        this.specialtyId = specialtyId;
        this.lastName = lastName;
        this.firstName = firstName;

    }

    @Override
    public int getId() {
        return id;
    }

    public int getSpecialtyId() {
        return specialtyId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }




    @Override
    public String toString() {
        return "Dr. " + firstName + " " + lastName;
    }
}