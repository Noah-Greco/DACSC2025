package HEPL.medecinJava.model.entity;

import java.io.Serializable;

public class Doctor implements Serializable {
    private int id;
    private int specialtyId;
    private String lastName;
    private String firstName;
    public Doctor(){}

    public Doctor(int specialtyId, String lastName, String firstName)
    {
        this .specialtyId = specialtyId;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public Doctor(int id, int specialtyId, String lastName, String firstName)
    {
        this(specialtyId, lastName, firstName);
        this.id = id;
    }

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    public int getSpecialtyId()
    {
        return specialtyId;
    }

    public void setSpecialtyId(int specialtyId)
    {
        this.specialtyId = specialtyId;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Override
    public String toString()
    {
        return "Infos Docteur : \n\t\tid = " + this.id + "\n\t\tId Specialité =  " + this.specialtyId +
                "\n\t\tPrénom = " + this.firstName + "\n\t\tNom =  " + this.lastName;
    }
}
