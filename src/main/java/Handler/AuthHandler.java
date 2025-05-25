package Handler;

import Controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.UserDto;
import exception.AlreadyExistValueException;
import exception.InvalidInputException;
import model.Customer;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final AuthController authcontroller = AuthController.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            sendErrorResponse(exchange, 415, "Unsupported Media Type"); return;
        }
        if (method.equals("POST") && path.equals("/auth/register")) {
            createUser(exchange);
        } else if (method.equals("POST") && path.equals("/auth/login")) {

        } else if (method.equals("GET") && path.equals("/auth/profile")) {

        } else if (method.equals("PUT") && path.equals("/auth/profile")) {

        } else if (method.equals("POSt") && path.equals("/auth/logout")) {

        } else {
            sendErrorResponse(exchange, 404, "Resource not found");
            return;
        }
    }

    public void createUser(HttpExchange exchange) throws IOException {

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
        try{
        UserDto userdto = new Gson().fromJson(body, UserDto.class);
        System.out.println("hello");
            userdto = authcontroller.register(userdto);
        Map<String, Object> responsebody = new HashMap<>();
        responsebody.put("message", "User registered successfully");
        responsebody.put("user_id", userdto.getUser_id());
        responsebody.put("token", userdto.getToken());
        String json = new Gson().toJson(responsebody);
        sendResponse(exchange, 200, json, "application/json");
        return;
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage()); return;
        } catch (AlreadyExistValueException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage()); return;
        } catch (Exception e) {
            System.out.println(e);
            sendErrorResponse(exchange, 500, "Internal Server Error"); return;
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
