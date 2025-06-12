// Create this new file: Handler/ItemHandler.java
package Handler;

import Controller.AuthController;
import Controller.ItemController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.ItemDto;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final ItemController itemController = new ItemController();
    private final ObjectMapper objectMapper;

    private final Pattern itemIdPattern = Pattern.compile("^/items/(\\d+)$");

    public ItemHandler() {
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
            authController.requireLogin(token);

            if (method.equals("POST") && path.equals("/items")) {
                handleSearchItems(exchange);
            } else if (method.equals("GET")) {
                Matcher matcher = itemIdPattern.matcher(path);
                if (matcher.matches()) {
                    String itemIdStr = matcher.group(1);
                    handleGetItemDetails(exchange, itemIdStr);
                } else {
                    sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource not found"));
                }
            } else {
                sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in ItemHandler: " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        }
    }

    private void handleSearchItems(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            ItemDto.ItemListRequestDTO filterDto = (body == null || body.isEmpty())
                    ? new ItemDto.ItemListRequestDTO(null, null, null)
                    : objectMapper.readValue(body, ItemDto.ItemListRequestDTO.class);

            List<ItemDto.FoodItemSchemaDTO> items = itemController.listItemsForBuyer(filterDto);
            sendResponse(exchange, 200, items);

        } catch (JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format: " + e.getOriginalMessage()));
        } catch (Exception e) {
            System.err.println("Error searching items: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error searching items."));
        }
    }

    private void handleGetItemDetails(HttpExchange exchange, String itemIdStr) throws IOException {
        int itemId;
        try {
            itemId = Integer.parseInt(itemIdStr);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Item ID format. Must be a number."));
            return;
        }

        Optional<ItemDto.FoodItemSchemaDTO> itemDtoOpt = itemController.getItemForBuyer(itemId);

        if (itemDtoOpt.isPresent()) {
            sendResponse(exchange, 200, itemDtoOpt.get());
        } else {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Item not found."));
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
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Error reading request body."));
            return null;
        }
        return jsonBody.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        if (exchange.getResponseCode() != -1) {
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
        }
    }
}
