package HEPL.reservationAdmin.classe;

public class Main {
    public static void main(String[] args) {
        try {
            ClientAdmin client = new ClientAdmin("192.168.2.128", 8090, 1500, 3000);

            String reponse = client.call("ACBP#ALL_CLIENT");
            System.out.println("Réponse brute : " + reponse);

            var liste = client.parseAllClient(reponse);
            for (ClientAdmin.ClientInfo c : liste) {
                System.out.println(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
