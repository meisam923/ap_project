package Handler;

import Controller.AuthController;
import Controller.OrderController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.OrderDto;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderHandler implements HttpHandler {

    private final AuthController authController = AuthController.getInstance();
    private final OrderController orderController = new OrderController();
    private final ObjectMapper objectMapper;

    private final Pattern orderIdPattern = Pattern.compile("^/orders/(\\d+)$");
    private final Pattern orderHistoryPattern = Pattern.compile("^/orders/history$");

    public OrderHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            String token = getJwtToken(exchange);
            if (token == null) return;
            User authenticatedUser = authController.requireLogin(token);

            if (method.equals("POST") && path.equals("/orders")) {
                handleSubmitOrder(exchange, authenticatedUser);
            } else if (method.equals("GET")) {
                Matcher orderIdMatcher = orderIdPattern.matcher(path);
                Matcher historyMatcher = orderHistoryPattern.matcher(path);

                if (orderIdMatcher.matches()) {
                    String orderIdStr = orderIdMatcher.group(1);
                    handleGetOrderDetails(exchange, orderIdStr, authenticatedUser);
                } else if (historyMatcher.matches()) {
                    handleGetOrderHistory(exchange, authenticatedUser);
                } else {
                    sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource not found"));
                }
            } else {
                sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
            }
        } catch (AuthController.AuthenticationException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in OrderHandler: " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        }
    }

    private void handleSubmitOrder(HttpExchange exchange, User user) throws IOException {
        try {
            String body = readRequestBody(exchange);
            if (body == null || body.isEmpty()) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Request body is required."));
                return;
            }

            OrderDto.SubmitOrderRequestDTO requestDto = objectMapper.readValue(body, OrderDto.SubmitOrderRequestDTO.class);
            Optional<OrderDto.OrderSchemaDTO> createdOrderOpt = orderController.submitOrder(requestDto, user);

            if (createdOrderOpt.isPresent()) {
                sendResponse(exchange, 200, createdOrderOpt.get());
            } else {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Failed to create order due to an internal issue."));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format: " + e.getOriginalMessage()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: " + e.getMessage()));
        } catch (SecurityException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO("Forbidden: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error submitting order: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error while submitting order: " + e.getMessage()));
        }
    }

    private void handleGetOrderDetails(HttpExchange exchange, String orderIdStr, User user) throws IOException {
        try {
            long orderId = Long.parseLong(orderIdStr);
            Optional<OrderDto.OrderSchemaDTO> orderDtoOpt = orderController.getOrderDetails(orderId, user);

            if (orderDtoOpt.isPresent()) {
                sendResponse(exchange, 200, orderDtoOpt.get());
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Order not found."));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Order ID format. Must be a number."));
        } catch (SecurityException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error getting order details: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error getting order details."));
        }
    }

    private void handleGetOrderHistory(HttpExchange exchange, User user) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);
            String searchFilter = queryParams.get("search");
            String vendorFilter = queryParams.get("vendor");
            List<OrderDto.OrderSchemaDTO> history = orderController.getOrderHistory(user, searchFilter, vendorFilter);
            sendResponse(exchange, 200, history);
        } catch (Exception e) {
            System.err.println("Error getting order history: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error getting order history."));
        }
    }

    // --- Helper Methods ---

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1 && !pair[1].isEmpty()) {
                try {
                    params.put(java.net.URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                            java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                } catch (Exception e) { /* Ignore */ }
            }
        }
        return params;
    }

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
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }
        return jsonBody.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        if (exchange.getResponseCode() != -1) return;
        String jsonResponse = objectMapper.writeValueAsString(responseObject);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) throws IOException {
        if (exchange.getResponseCode() == -1) {
            sendResponse(exchange, statusCode, errorDto);
        }
    }
}
