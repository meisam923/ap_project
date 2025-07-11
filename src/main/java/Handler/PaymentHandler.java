package Handler;

import Controller.AuthController;
import Controller.PaymentController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.PaymentDto;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PaymentHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final PaymentController paymentController = new PaymentController();
    private final ObjectMapper objectMapper;

    public PaymentHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            User authenticatedUser = getUserFromToken(exchange);
            if (authenticatedUser == null) return;

            if (method.equals("GET") && path.equals("/transactions")) {
                handleGetTransactions(exchange, authenticatedUser);
            } else if (method.equals("POST") && path.equals("/wallet/top-up")) {
                handleWalletTopUp(exchange, authenticatedUser);
            } else if (method.equals("POST") && path.equals("/payment/online")) {
                handleOnlinePayment(exchange, authenticatedUser);
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Not Found"));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (SecurityException e) {
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (PaymentController.NotFoundException e) {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (PaymentController.ConflictException e) {
            sendErrorResponse(exchange, 409, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (IllegalArgumentException | JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in PaymentHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleGetTransactions(HttpExchange exchange, User user) throws IOException {
        sendResponse(exchange, 200, paymentController.getTransactionHistory(user));
    }

    private void handleWalletTopUp(HttpExchange exchange, User user) throws IOException {
        String body = readRequestBody(exchange);
        PaymentDto.TopUpRequestDTO requestDto = objectMapper.readValue(body, PaymentDto.TopUpRequestDTO.class);
        paymentController.topUpWallet(user, requestDto);
        sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Wallet topped up successfully."));
    }

    private void handleOnlinePayment(HttpExchange exchange, User user) throws IOException {
        String body = readRequestBody(exchange);
        PaymentDto.PaymentRequestDTO requestDto = objectMapper.readValue(body, PaymentDto.PaymentRequestDTO.class);
        PaymentDto.TransactionSchemaDTO responseDto = paymentController.processPayment(user, requestDto);
        sendResponse(exchange, 200, responseDto);
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