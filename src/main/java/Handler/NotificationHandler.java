package Handler;

import Controller.AuthController;
import Controller.NotificationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.NotificationDto;
import dto.UserDto;
import model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NotificationHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final NotificationController notificationController = new NotificationController();
    private final ObjectMapper objectMapper;

    public NotificationHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            try {
                String token = getJwtToken(exchange);
                if (token == null) return;

                User user = authController.requireLogin(token);

                List<NotificationDto.NotificationSchemaDTO> notifications = notificationController.getNotificationsForUser(user);
                sendResponse(exchange, 200, notifications);

            } catch (Exception e) {
                System.err.println("Error in NotificationHandler: " + e.getMessage());
                e.printStackTrace();
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        } else {
            sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
        }
    }

    private String getJwtToken(HttpExchange exchange) throws IOException {
        String tokenHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (tokenHeader == null || !tokenHeader.toLowerCase().startsWith("bearer ")) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Missing or malformed Bearer token."));
            return null;
        }
        return tokenHeader.substring(7);
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

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) throws IOException {
        if (exchange.getResponseCode() == -1) {
            sendResponse(exchange, statusCode, errorDto);
        }
    }
}