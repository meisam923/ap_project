package Handler;

import Controller.AuthController;
import Controller.RatingController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.RatingDto;
import dto.UserDto;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RatingHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final RatingController ratingController = new RatingController();
    private final ObjectMapper objectMapper;

    private final Pattern ratingsPattern = Pattern.compile("^/ratings$");
    private final Pattern itemsRatingPattern = Pattern.compile("^/ratings/items/(\\d+)$");
    private final Pattern ratingIdPattern = Pattern.compile("^/ratings/(\\d+)$");

    public RatingHandler() {
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

            Matcher itemsMatcher = itemsRatingPattern.matcher(path);
            Matcher ratingIdMatcher = ratingIdPattern.matcher(path);

            if (method.equals("POST") && ratingsPattern.matcher(path).matches()) {
                handleSubmitRating(exchange, authenticatedUser);
            } else if (method.equals("GET") && itemsMatcher.matches()) {
                int itemId = Integer.parseInt(itemsMatcher.group(1));
                handleGetItemRatings(exchange, itemId);
            } else if (ratingIdMatcher.matches()) {
                long ratingId = Long.parseLong(ratingIdMatcher.group(1));
                if (method.equals("GET")) {
                    handleGetRatingById(exchange, ratingId);
                } else if (method.equals("PUT")) {
                    handleUpdateRating(exchange, ratingId, authenticatedUser);
                } else if (method.equals("DELETE")) {
                    handleDeleteRating(exchange, ratingId, authenticatedUser);
                } else {
                    sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
                }
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Not Found"));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (SecurityException e) {
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (RatingController.NotFoundException e) {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (RatingController.ConflictException e) {
            sendErrorResponse(exchange, 409, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: " + e.getMessage()));
        } catch (JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format: " + e.getOriginalMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in RatingHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleSubmitRating(HttpExchange exchange, User user) throws IOException {
        String body = readRequestBody(exchange);
        RatingDto.SubmitRatingRequestDTO requestDto = objectMapper.readValue(body, RatingDto.SubmitRatingRequestDTO.class);
        RatingDto.RatingSchemaDTO responseDto = ratingController.submitRating(requestDto, user);
        sendResponse(exchange, 200, responseDto);
    }

    private void handleGetItemRatings(HttpExchange exchange, int itemId) throws IOException {
        RatingDto.ItemRatingsResponseDTO responseDto = ratingController.getRatingsForItem(itemId);
        sendResponse(exchange, 200, responseDto);
    }

    private void handleGetRatingById(HttpExchange exchange, long ratingId) throws IOException {
        Optional<RatingDto.RatingSchemaDTO> ratingOpt = ratingController.getRatingById(ratingId);
        if (ratingOpt.isPresent()) {
            sendResponse(exchange, 200, ratingOpt.get());
        } else {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Rating not found."));
        }
    }

    private void handleUpdateRating(HttpExchange exchange, long ratingId, User user) throws IOException {
        String body = readRequestBody(exchange);
        RatingDto.UpdateRatingRequestDTO requestDto = objectMapper.readValue(body, RatingDto.UpdateRatingRequestDTO.class);
        RatingDto.RatingSchemaDTO responseDto = ratingController.updateRating(ratingId, requestDto, user);
        sendResponse(exchange, 200, responseDto);
    }

    private void handleDeleteRating(HttpExchange exchange, long ratingId, User user) throws IOException {
        ratingController.deleteRating(ratingId, user);
        sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Rating deleted successfully."));
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