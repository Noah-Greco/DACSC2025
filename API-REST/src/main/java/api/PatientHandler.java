package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.dao.PatientDAO;
import server.util.HttpUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class PatientHandler implements HttpHandler {
    private final PatientDAO dao;

    public PatientHandler() throws SQLException {
        this.dao = new PatientDAO();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            HttpUtils.addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                String body = HttpUtils.readRequestBody(exchange);

                String lastName = HttpUtils.extractJsonValue(body, "lastName");
                String firstName = HttpUtils.extractJsonValue(body, "firstName");
                String newPatientStr = HttpUtils.extractJsonValue(body, "newPatient");
                String patientIdStr = HttpUtils.extractJsonValue(body, "patientId");
                String birthDateStr = HttpUtils.extractJsonValue(body, "birthDate");

                boolean isNew = Boolean.parseBoolean(newPatientStr);

                int id;

                if (isNew) {
                    if (birthDateStr == null) {
                        HttpUtils.sendResponse(exchange, 400, "{\"error\":\"birthDate manquante\"}");
                        return;
                    }

                    LocalDate birthDate = LocalDate.parse(birthDateStr);

                    int existingId = dao.findPatientId(lastName, firstName, birthDate);
                    if (existingId != -1) {
                        HttpUtils.sendResponse(exchange, 409, "{\"error\":\"Ce patient existe déjà\"}");
                        return;
                    }

                    id = dao.createPatient(lastName, firstName, birthDate);

                } else {
                    if (patientIdStr == null) {
                        HttpUtils.sendResponse(exchange, 400, "{\"error\":\"patientId manquant\"}");
                        return;
                    }

                    int pid = Integer.parseInt(patientIdStr);
                    boolean ok = dao.verifyPatientById(pid, lastName, firstName);

                    if (!ok) {
                        HttpUtils.sendResponse(exchange, 404, "{\"error\":\"Patient introuvable\"}");
                        return;
                    }

                    id = pid;
                }

                HttpUtils.sendResponse(exchange, 200, "{\"id\":" + id + "}");

            } catch (Exception e) {
                e.printStackTrace();

                HttpUtils.sendResponse(exchange, 500, "{\"error\":\"Erreur serveur\"}");
            }

        }
    }
}