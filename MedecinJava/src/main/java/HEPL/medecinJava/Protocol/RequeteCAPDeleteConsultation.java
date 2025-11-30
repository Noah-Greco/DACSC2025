package HEPL.medecinJava.Protocol;

public class RequeteCAPDeleteConsultation extends RequeteCAP {

    private final int idConsultation;

    public RequeteCAPDeleteConsultation(int idConsultation) {
        this.idConsultation = idConsultation;
    }

    public int getIdConsultation() { return idConsultation; }

    @Override
    public String toString() {
        return "RequeteCAPDeleteConsultation{id=" + idConsultation + "}";
    }
}
