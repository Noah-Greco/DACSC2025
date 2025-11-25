package HEPL.medecinJava.model.viewmodel;

public class DoctorSearchVM {
    private Integer id;
    private Integer specialtyId;
    private String lastName;
    private String firstName;

    public DoctorSearchVM() {}

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getSpecialtyId()
    {
        return specialtyId;
    }

    public void setSpecialtyId(Integer specialtyId)
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
}
