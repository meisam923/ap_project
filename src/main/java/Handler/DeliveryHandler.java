package Handler;

import Controller.AuthController;
import Controller.DeliveryController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.DeliveryDto;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeliveryHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final DeliveryController deliveryController = new DeliveryController();
    private final ObjectMapper objectMapper;

    private final Pattern availablePattern = Pattern.compile("^/deliveries/available$");
    private final Pattern historyPattern = Pattern.compile("^/deliveries/history$");
    private final Pattern orderIdPattern = Pattern.compile("^/deliveries/(\\d+)$");

    public DeliveryHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            User courier = getUserFromToken(exchange);
            if (courier == null) return;

            if (method.equals("GET") && availablePattern.matcher(path).matches()) {
                handleGetAvailableDeliveries(exchange, courier);
            } else if (method.equals("GET") && historyPattern.matcher(path).matches()) {
                handleGetDeliveryHistory(exchange, courier);
            } else if (method.equals("PATCH")) {
                Matcher matcher = orderIdPattern.matcher(path);
                if (matcher.matches()) {
                    long orderId = Long.parseLong(matcher.group(1));
                    handleUpdateDeliveryStatus(exchange, orderId, courier);
                } else {
                    sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Not Found"));
                }
            } else {
                sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (SecurityException e) {
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (DeliveryController.NotFoundException e) {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (DeliveryController.ConflictException e) {
            sendErrorResponse(exchange, 409, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (DeliveryController.InvalidInputException | JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in DeliveryHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleGetAvailableDeliveries(HttpExchange exchange, User courier) throws IOException {
        sendResponse(exchange, 200, deliveryController.getAvailableDeliveries(courier));
    }

    private void handleGetDeliveryHistory(HttpExchange exchange, User courier) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);
        String searchFilter = queryParams.get("search");
        String vendorFilter = queryParams.get("vendor");
        sendResponse(exchange, 200, deliveryController.getDeliveryHistory(courier, searchFilter, vendorFilter));
    }

    private void handleUpdateDeliveryStatus(HttpExchange exchange, long orderId, User courier) throws IOException {
        String body = readRequestBody(exchange);
        DeliveryDto.UpdateDeliveryStatusRequestDTO requestDto = objectMapper.readValue(body, DeliveryDto.UpdateDeliveryStatusRequestDTO.class);
        if (requestDto.status() == null || requestDto.status().isBlank()) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: 'status' field is required."));
            return;
        }
        sendResponse(exchange, 200, deliveryController.updateDeliveryStatus(orderId, requestDto.status(), courier));
    }

    // --- Helper Methods (Self-Contained) ---
    private String getJwtToken(HttpExchange exchange) throws IOException {
        String tokenHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (tokenHeader == null || !tokenHeader.toLowerCase().startsWith("bearer ")) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Missing or malformed Bearer token."));
            return null;
        }
        return tokenHeader.substring(7);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) jsonBody.append(line);
        }
        return jsonBody.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        if (exchange.getResponseCode() != -1) return;
        String jsonResponse = objectMapper.writeValueAsString(responseObject);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(responseBytes); }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) {
        try {
            if (exchange.getResponseCode() == -1) sendResponse(exchange, statusCode, errorDto);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1 && !pair[1].isEmpty()) params.put(pair[0], pair[1]);
        }
        return params;
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
}