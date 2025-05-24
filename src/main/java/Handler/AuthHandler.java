package Handler;

import Controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Customer;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final AuthController authService = AuthController.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            sendErrorResponse(exchange, 415, "Unsupported Media Type");
        }
        if (method.equals("POST") && path.equals("/auth/register")) {
            createUser(exchange);
        }
    }
    public void createUser(HttpExchange exchange) throws IOException {

        Map mapper = new ObjectMapper().readValue(exchange.getRequestBody(), Map.class);

        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        } catch (IOException e) {
            sendErrorResponse(exchange, 500, "Internal Server Error");
            return;
        }
        String body = jsonBody.toString();
        System.out.println("Received JSON: " + body);
        String response = "";

        User user;

        if (mapper.containsKey("role") && mapper.get("role").equals("customer")) {
            user = new Gson().fromJson(body, Customer.class);
        }

    }


    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBody.getBytes());
        }
    }


    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        String jsonError = String.format("{ \"error\": \"%s\" }", errorMessage);
        sendResponse(exchange, statusCode, jsonError, "application/json");
    }
}
