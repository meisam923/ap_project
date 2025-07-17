package Handler;

import Controller.AuthController;
import Controller.FavoriteController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.RestaurantDto;
import dto.UserDto;
import exception.NotFoundException;
import model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FavoriteHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final FavoriteController favoriteController = new FavoriteController();
    private final ObjectMapper objectMapper;

    private final Pattern favoriteIdPattern = Pattern.compile("^/favorites/(\\d+)$");

    public FavoriteHandler() {
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

            if (method.equals("GET") && path.equals("/favorites")) {
                handleGetFavorites(exchange, authenticatedUser);
            } else {
                Matcher matcher = favoriteIdPattern.matcher(path);
                if (matcher.matches()) {
                    int restaurantId = Integer.parseInt(matcher.group(1));
                    if (method.equals("PUT")) {
                        handleAddFavorite(exchange, authenticatedUser, restaurantId);
                    } else if (method.equals("DELETE")) {
                        handleRemoveFavorite(exchange, authenticatedUser, restaurantId);
                    } else {
                        sendErrorResponse(exchange, 405, new UserDto.ErrorResponseDTO("Method Not Allowed"));
                    }
                } else {
                    sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Not Found"));
                }
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (SecurityException e) {
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unhandled error in FavoriteHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleGetFavorites(HttpExchange exchange, User user) throws IOException {
        List<RestaurantDto.RestaurantSchemaDTO> favorites = favoriteController.getFavorites(user);
        sendResponse(exchange, 200, favorites);
    }

    private void handleAddFavorite(HttpExchange exchange, User user, int restaurantId) throws Exception, NotFoundException {
        favoriteController.addFavorite(user, restaurantId);
        sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Restaurant added to favorites."));
    }

    private void handleRemoveFavorite(HttpExchange exchange, User user, int restaurantId) throws IOException {
        favoriteController.removeFavorite(user, restaurantId);
        sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Restaurant removed from favorites."));
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
