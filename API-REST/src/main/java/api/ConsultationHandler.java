package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import common.entity.Consultation;
import common.protocol.UpdateConsultationRequest;
import server.dao.ConsultationDAO;
import server.util.HttpUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ConsultationHandler implements HttpHandler {

    private final ConsultationDAO dao;

    public ConsultationHandler() throws SQLException {
        this.dao = new ConsultationDAO();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // 1. Gérer le CORS (OPTIONS)
        if (method.equalsIgnoreCase("OPTIONS")) {
            HttpUtils.addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            switch (method.toUpperCase()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    HttpUtils.sendResponse(exchange, 405, "{\"error\":\"Methode non autorisee\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> params = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());

        // --- Paramètres REST attendus par l’énoncé ---
        String doctor = params.get("doctor");         // nom (last_name)
        String specialty = params.get("specialty");   // nom de spécialité

        LocalDate date = null;
        if (params.containsKey("date") && params.get("date") != null && !params.get("date").isBlank()) {
            date = LocalDate.parse(params.get("date"));
        }

        Integer patientId = null;
        if (params.containsKey("patientId") && params.get("patientId") != null && !params.get("patientId").isBlank()) {
            patientId = Integer.parseInt(params.get("patientId"));
        }

        // --- Appel DAO conforme ---
        // Règle: si patientId == null => renvoyer UNIQUEMENT les créneaux libres (patient_id IS NULL)
        List<Consultation> list = dao.searchConsultationsRest(doctor, specialty, date, patientId);

        // --- Conversion JSON Manuelle ---
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Consultation c = list.get(i);
            sb.append("{")
                    .append("\"id\":").append(c.getId()).append(",")
                    .append("\"doctorId\":").append(c.getDoctorId()).append(",")
                    // dans ton modèle actuel, 0 signifie "null"
                    .append("\"patientId\":").append(c.getPatientId() == 0 ? "null" : c.getPatientId()).append(",")
                    .append("\"date\":\"").append(c.getDate()).append("\",")
                    .append("\"hour\":\"").append(c.getHour()).append("\",")
                    .append("\"reason\":\"").append(c.getReason() == null ? "" : escapeJson(c.getReason())).append("\"")
                    .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");

        HttpUtils.sendResponse(exchange, 200, sb.toString());
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);

        int doctorId = Integer.parseInt(HttpUtils.extractJsonValue(body, "doctorId"));
        LocalDate date = LocalDate.parse(HttpUtils.extractJsonValue(body, "date"));
        String startTime = HttpUtils.extractJsonValue(body, "startTime");
        int duration = Integer.parseInt(HttpUtils.extractJsonValue(body, "duration"));
        int number = Integer.parseInt(HttpUtils.extractJsonValue(body, "number"));

        int count = dao.addConsultations(doctorId, date, startTime, duration, number);
        HttpUtils.sendResponse(exchange, 201, "{\"message\":\"" + count + " consultations creees\"}");
    }

    private void handlePut(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        Map<String, String> params = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());

        int id = params.containsKey("id") ? Integer.parseInt(params.get("id")) : -1;
        if (id == -1) {
            String idStr = HttpUtils.extractJsonValue(body, "consultationId");
            if (idStr != null) id = Integer.parseInt(idStr);
        }

        UpdateConsultationRequest req = new UpdateConsultationRequest();
        req.setConsultationId(id);

        String patientIdStr = HttpUtils.extractJsonValue(body, "patientId");
        if (patientIdStr != null) {
            req.setPatientId(Integer.parseInt(patientIdStr));
            req.setReason(HttpUtils.extractJsonValue(body, "reason"));
        }

        if (dao.updateConsultation(req)) {
            HttpUtils.sendResponse(exchange, 200, "{\"message\":\"Succes\"}");
        } else {
            HttpUtils.sendResponse(exchange, 400, "{\"error\":\"Echec de la mise a jour\"}");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> params = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        if (params.containsKey("id")) {
            int id = Integer.parseInt(params.get("id"));
            if (dao.deleteConsultation(id)) {
                HttpUtils.sendResponse(exchange, 200, "{\"message\":\"Annulation reussie\"}");
            } else {
                HttpUtils.sendResponse(exchange, 404, "{\"error\":\"Consultation introuvable\"}");
            }
        } else {
            HttpUtils.sendResponse(exchange, 400, "{\"error\":\"ID manquant\"}");
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}