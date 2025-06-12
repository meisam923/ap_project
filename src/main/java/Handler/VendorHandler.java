package Handler;

import Controller.AuthController;
import Controller.VendorController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.UserDto;
import dto.VendorDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VendorHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final VendorController vendorController = new VendorController();
    private final ObjectMapper objectMapper;

    private final Pattern vendorIdPattern = Pattern.compile("^/vendors/(\\d+)$");

    public VendorHandler() {
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
            if (token == null) {
                return;
            }
            User authenticatedUser = authController.requireLogin(token);

            if (method.equals("POST") && path.equals("/vendors")) {
                handleListVendors(exchange, authenticatedUser);
            } else if (method.equals("GET")) {
                Matcher matcher = vendorIdPattern.matcher(path);
                if (matcher.matches()) {
                    String vendorIdStr = matcher.group(1);
                    handleGetVendorMenu(exchange, vendorIdStr, authenticatedUser);
                } else {
                    sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource not found"));
                }
            } else {
                sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in VendorHandler: " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        }
    }

    private void handleListVendors(HttpExchange exchange, User user) throws IOException {
        try {
            String body = readRequestBody(exchange);
            VendorDto.VendorListRequestDTO filterDto = (body == null || body.isEmpty())
                    ? new VendorDto.VendorListRequestDTO(null, null)
                    : objectMapper.readValue(body, VendorDto.VendorListRequestDTO.class);

            List<VendorDto.RestaurantSchemaDTO> vendors = vendorController.listVendorsForBuyer(filterDto);
            sendResponse(exchange, 200, vendors);
        } catch (JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format: " + e.getOriginalMessage()));
        } catch (Exception e) {
            System.err.println("Error listing vendors: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error listing vendors."));
        }
    }

    private void handleGetVendorMenu(HttpExchange exchange, String vendorIdStr, User user) throws IOException {
        long vendorId;
        try {
            vendorId = Long.parseLong(vendorIdStr);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Vendor ID format. Must be a number."));
            return;
        }

        try {
            VendorDto.VendorMenuResponseDTO menuResponse = vendorController.getVendorMenuForBuyer(vendorId);
            if (menuResponse == null) {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Vendor not found."));
                return;
            }
            sendResponse(exchange, 200, menuResponse);
        } catch (Exception e) {
            System.err.println("Error getting vendor menu: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error getting vendor menu."));
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

    private String readRequestBody(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            return "";
        }
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading request body: " + e.getMessage());
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Error reading request body."));
            return null;
        }
        return jsonBody.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        if (exchange.getResponseCode() != -1) {
            System.err.println("WARNING: Response already sent. Cannot send new response.");
            return;
        }
        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(responseObject);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to serialize response object: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error: Failed to generate response."));
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) throws IOException {
        if (exchange.getResponseCode() == -1) {
            sendResponse(exchange, statusCode, errorDto);
        } else {
            System.err.println("Attempted to send error but response was already committed.");
        }
    }
}
