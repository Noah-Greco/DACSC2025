package api;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class MainAPI {

    public static void main(String[] args) {
        try {
            System.out.println("Démarrage de l'API REST sur le port 8080...");

            // Création du serveur
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // On associe l'URL "/api/consultations" à notre Handler séparé
            // 1. Consultations
            server.createContext("/api/consultations", new ConsultationHandler());
            // 2. Patients (Login)
            server.createContext("/api/patients", new PatientHandler());

            // 3. Médecins (Recherche)
            server.createContext("/api/doctors", new DoctorHandler());

            server.createContext("/api/specialties", new SpecialtyHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("Serveur prêt ! En attente de requêtes...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}