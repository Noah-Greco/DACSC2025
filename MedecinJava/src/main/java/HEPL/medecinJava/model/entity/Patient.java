package HEPL.medecinJava.model.entity;

import java.io.Serializable;
import java.time.LocalDate;

public class Patient implements Serializable {
    private Integer  id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    public Patient(){}

    public Patient(String firstName, String lastName, LocalDate birthDate)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }
    public Patient(int id, String firstName, String lastName, LocalDate birthDate)
    {
        this(firstName, lastName, birthDate);
        this.id = id;
    }
    public Integer getId()
    {
        return id;
    }
    public void setId(Integer id)
    {
        this.id = id;
    }
    public String getFirstName()
    {
        return firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    public LocalDate getBirthDate()
    {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate)
    {
        this.birthDate = birthDate;
    }

    @Override
    public String toString()
    {
        return "Infos Patient : \n\t\tid = " + this.id + "\n\t\tAnnif =  " + this.birthDate +
                "\n\t\tPrénom = " + this.firstName + "\n\t\tNom =  " + this.lastName;
    }
}
