package Handler;

import Controller.AuthController;
import Controller.CouponController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.CouponDto;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CouponHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final CouponController couponController = new CouponController();
    private final ObjectMapper objectMapper;

    public CouponHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            String token = getJwtToken(exchange);
            if (token == null) return;
            authController.requireLogin(token);

            if ("GET".equals(method) && path.equals("/coupons")) {
                Map<String, String> queryParams = parseQueryParams(query);
                if (queryParams.containsKey("coupon_code")) {
                    handleValidateCoupon(exchange, queryParams.get("coupon_code"));
                } else {
                    sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Bad Request: 'coupon_code' parameter is required."));
                }
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource not found"));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in CouponHandler: " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        }
    }

    private void handleValidateCoupon(HttpExchange exchange, String couponCode) throws IOException {
        if (couponCode == null || couponCode.isBlank()) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: 'coupon_code' query parameter cannot be empty."));
            return;
        }

        try {
            Optional<CouponDto.CouponSchemaDTO> couponDtoOpt = couponController.getValidCouponByCode(couponCode);

            if (couponDtoOpt.isPresent()) {
                sendResponse(exchange, 200, couponDtoOpt.get());
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Not Found: Coupon code is invalid or does not exist."));
            }
        } catch (Exception e) {
            System.err.println("Error validating coupon '" + couponCode + "': " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error while checking coupon."));
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1) {
                params.put(pair[0], pair[1]);
            } else {
                params.put(pair[0], "");
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