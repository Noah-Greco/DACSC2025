package server.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    // Ajoute les headers CORS (utilisé partout)
    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // Envoie la réponse JSON
    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // On s'assure que les headers CORS sont là
        if (!exchange.getResponseHeaders().containsKey("Access-Control-Allow-Origin")) {
            addCorsHeaders(exchange);
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Lit le body de la requête (pour POST/PUT)
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    // Extrait une valeur d'un JSON simple "clé": "valeur"
    public static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();

        // On saute les espaces et les guillemets éventuels
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }

        int end = start;
        // On lit jusqu'à la fin de la valeur (guillemet, virgule ou accolade fermante)
        while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
            end++;
        }

        return json.substring(start, end).trim();
    }

    // Parse les paramètres d'URL (ex: ?id=5&name=toto)
    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> res = new HashMap<>();
        if (query == null) return res;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                res.put(pair[0], pair[1]);
            }
        }
        return res;
    }
}