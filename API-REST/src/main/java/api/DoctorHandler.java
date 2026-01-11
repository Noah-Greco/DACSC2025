package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import common.entity.Doctor;
import server.dao.DoctorDAO;
import server.util.HttpUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DoctorHandler implements HttpHandler {

    private final DoctorDAO dao;

    public DoctorHandler() throws SQLException {
        this.dao = new DoctorDAO();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            HttpUtils.addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                List<Doctor> list = dao.getAllDoctors();

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Doctor d = list.get(i);
                    json.append("{");
                    json.append("\"id\":").append(d.getId()).append(",");
                    json.append("\"lastName\":\"").append(d.getLastName()).append("\",");
                    json.append("\"firstName\":\"").append(d.getFirstName()).append("\",");
                    json.append("\"specialtyId\":").append(d.getSpecialtyId());
                    json.append("}");
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");

                HttpUtils.sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                e.printStackTrace();
                HttpUtils.sendResponse(exchange, 500, "{\"error\": \"Erreur interne BDD\"}");
            }
        } else {
            HttpUtils.sendResponse(exchange, 405, "{\"error\": \"Méthode non autorisée\"}");
        }
    }
}