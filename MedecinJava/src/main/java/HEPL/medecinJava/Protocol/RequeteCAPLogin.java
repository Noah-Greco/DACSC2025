package HEPL.medecinJava.Protocol;

public class RequeteCAPLogin extends RequeteCAP {

    private final int doctorId;
    private final String password;

    public RequeteCAPLogin(int doctorId, String password) {
        this.doctorId = doctorId;
        this.password = password;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "RequeteCAPLogin{doctorId=" + doctorId + "}";
    }
}
