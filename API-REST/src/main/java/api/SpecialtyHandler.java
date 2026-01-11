package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import common.entity.Specialty;
import server.dao.SpecialtyDAO;
import server.util.HttpUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SpecialtyHandler implements HttpHandler {

    private final SpecialtyDAO dao;

    public SpecialtyHandler() throws SQLException {
        this.dao = new SpecialtyDAO();
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
                List<Specialty> list = dao.getAllSpecialties();

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Specialty s = list.get(i);
                    json.append("{");
                    json.append("\"id\":").append(s.getId()).append(",");
                    json.append("\"name\":\"").append(s.getName()).append("\"");
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