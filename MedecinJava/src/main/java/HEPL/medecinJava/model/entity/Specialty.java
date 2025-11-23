package HEPL.medecinJava.model.entity;

import java.io.Serializable;

public class Specialty implements Serializable {
    private int id;
    private String name;

    public Specialty(){}

    public Specialty(String name)
    {
        this.name = name;
    }

    public Specialty(int id, String name)
    {
        this(name);
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Infos Spécialité : \n\t\tid = " + this.id + "\n\t\tNom =  " + this.name;
    }
}
