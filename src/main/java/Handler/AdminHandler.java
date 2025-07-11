package Handler;

import Controller.AdminController;
import Controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AdminHandler implements HttpHandler {
    private final AdminController adminController = new AdminController();
    private final AuthController authController = AuthController.getInstance();
    private final ObjectMapper objectMapper;

    public AdminHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            User adminUser = getUserFromToken(exchange);
            if (adminUser == null) return;

            if (method.equals("GET") && path.equals("/admin/users")) {
                handleGetAllUsers(exchange, adminUser);
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Admin resource not found."));
            }

        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (AdminController.SecurityException e) {
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in AdminHandler: " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        }
    }

    private void handleGetAllUsers(HttpExchange exchange, User adminUser) throws IOException {
        sendResponse(exchange, 200, adminController.getAllUsers(adminUser));
    }


    private User getUserFromToken(HttpExchange exchange) throws AuthController.AuthenticationException, IOException {
        String tokenHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (tokenHeader == null || !tokenHeader.toLowerCase().startsWith("bearer ")) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Missing or malformed Bearer token."));
            return null;
        }
        String token = tokenHeader.substring(7);
        return authController.requireLogin(token);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        if (exchange.getResponseCode() != -1) return;
        String jsonResponse = objectMapper.writeValueAsString(responseObject);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) {
        try {
            if (exchange.getResponseCode() == -1) {
                sendResponse(exchange, statusCode, errorDto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}