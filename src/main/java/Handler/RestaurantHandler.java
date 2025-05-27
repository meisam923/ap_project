package Handler;

import Controller.AuthController;
import Controller.RestaurantController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.RestaurantDto;
import enums.RestaurantStatus;
import enums.Role;
import exception.InvalidInputException;
import model.Owner;
import model.Restaurant;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // For placeholder responses
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestaurantHandler implements HttpHandler {
    // Controllers and helpers
    private final AuthController authController = AuthController.getInstance();
    private final RestaurantController restaurantController = new RestaurantController();
    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<Route> routes = new ArrayList<>();

    record Route(String httpMethod, Pattern regexPattern, BiConsumer<HttpExchange, Matcher> action) {}

    public RestaurantHandler() {
        // === Routes STRICTLY from your OpenAPI Specification ===

        // POST /restaurants
        routes.add(new Route("POST", Pattern.compile("^/restaurants$"), (exchange, matcher) ->
                createRestaurantAction(exchange)
        ));

        // GET /restaurants/mine
        routes.add(new Route("GET", Pattern.compile("^/restaurants/mine$"), (exchange, matcher) ->
                getSellerRestaurantAction(exchange)
        ));

        // PUT /restaurants/{id}
        routes.add(new Route("PUT", Pattern.compile("^/restaurants/(?<id>\\d+)$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            updateRestaurantAction(exchange, restaurantId);
        }));

        // POST /restaurants/{id}/item (Add food item to restaurant)
        routes.add(new Route("POST", Pattern.compile("^/restaurants/(?<id>\\d+)/item$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            addRestaurantItemAction(exchange, restaurantId);
        }));

        // PUT /restaurants/{id}/item/{item_id} (Edit food item)
        routes.add(new Route("PUT", Pattern.compile("^/restaurants/(?<id>\\d+)/item/(?<itemid>\\d+)$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            String itemId = matcher.group("itemid");
            updateRestaurantItemAction(exchange, restaurantId, itemId);
        }));

        // DELETE /restaurants/{id}/item/{item_id} (Delete item)
        routes.add(new Route("DELETE", Pattern.compile("^/restaurants/(?<id>\\d+)/item/(?<itemid>\\d+)$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            String itemId = matcher.group("itemid");
            deleteRestaurantItemAction(exchange, restaurantId, itemId);
        }));

        // POST /restaurants/{id}/menu (Add restaurant menu)
        routes.add(new Route("POST", Pattern.compile("^/restaurants/(?<id>\\d+)/menu$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            addRestaurantMenuAction(exchange, restaurantId);
        }));

        // DELETE /restaurants/{id}/menu/{title} (Delete restaurant menu)
        routes.add(new Route("DELETE", Pattern.compile("^/restaurants/(?<id>\\d+)/menu/(?<title>[^/]+)$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            String title = matcher.group("title");
            deleteRestaurantMenuAction(exchange, restaurantId, title);
        }));

        // PUT /restaurants/{id}/menu/{title} (Add item to a menu - item_id in request body)
        routes.add(new Route("PUT", Pattern.compile("^/restaurants/(?<id>\\d+)/menu/(?<title>[^/]+)$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            String title = matcher.group("title");
            addItemToRestaurantMenuAction(exchange, restaurantId, title);
        }));

        // DELETE /restaurants/{id}/menu/{title}/{item_id} (Delete item from restaurant menu)
        routes.add(new Route("DELETE", Pattern.compile("^/restaurants/(?<id>\\d+)/menu/(?<title>[^/]+)/(?<itemid>\\d+)$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            String title = matcher.group("title");
            String itemId = matcher.group("itemid");
            removeItemFromRestaurantMenuAction(exchange, restaurantId, title, itemId);
        }));

        // GET /restaurants/{id}/orders (Get list of orders for a specific restaurant)
        routes.add(new Route("GET", Pattern.compile("^/restaurants/(?<id>\\d+)/orders$"), (exchange, matcher) -> {
            String restaurantId = matcher.group("id");
            listRestaurantOrdersAction(exchange, restaurantId);
        }));

        // PATCH /restaurants/orders/{order_id} (Change status of an order for a restaurant)
        routes.add(new Route("PATCH", Pattern.compile("^/restaurants/orders/(?<orderid>\\d+)$"), (exchange, matcher) -> {
            String orderId = matcher.group("orderid");
            patchRestaurantOrderAction(exchange, orderId);
        }));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        Route matchedRoute = null;
        Matcher pathMatcher = null;

        for (Route route : routes) {
            Matcher currentMatcher = route.regexPattern().matcher(requestPath);
            if (currentMatcher.matches()) {
                if (route.httpMethod().equalsIgnoreCase(requestMethod)) {
                    matchedRoute = route;
                    pathMatcher = currentMatcher;
                    break;
                }
                if (pathMatcher == null) {
                    pathMatcher = currentMatcher;
                }
            }
        }

        if (matchedRoute != null) {
            try {
                matchedRoute.action().accept(exchange, pathMatcher);
            } catch (Exception e) {
                System.err.println("Error executing route action for " + requestMethod + " " + requestPath + ": " + e.getMessage());
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
            if (pathMatcher != null) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
            } else {
                sendErrorResponse(exchange, 404, "Not Found");
            }
        }
    }

    // --- Action Methods (Your existing + Placeholders for the specified routes) ---

    private void createRestaurantAction(HttpExchange exchange) {
        try {
            checkMediaType(exchange);
            String token_header = exchange.getRequestHeaders().getFirst("Authorization");
            if (token_header == null || !token_header.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); return;
            }
            String token = token_header.substring(7);
            User user = authController.requireLogin(token);
            if (!user.getRole().equals(Role.SELLER)) {
                sendErrorResponse(exchange, 403, "Forbidden request"); return;
            }
            if (((Owner)user).getRestaurant() != null) {
                sendErrorResponse(exchange,409,"Conflict occurred: Seller already has a restaurant"); return;
            }
            String jsonBody = readRequestBody(exchange);
            if (jsonBody == null) return;

            RestaurantDto.RegisterRestaurantDto restaurantDto = objectMapper.readValue(jsonBody, RestaurantDto.RegisterRestaurantDto.class);
            RestaurantDto.RegisterReponseRestaurantDto response = restaurantController.createRestaurant(restaurantDto, (Owner) user);
            sendResponse(exchange, 201, gson.toJson(response), "application/json");
        } catch (JsonSyntaxException | com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage()); e.printStackTrace();
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException in createRestaurantAction: " + e.getMessage()); e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurantAction: " + e.getMessage()); e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void getSellerRestaurantAction(HttpExchange exchange) {
        try {
            String token_header = exchange.getRequestHeaders().getFirst("Authorization");
            if (token_header == null || !token_header.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); return;
            }
            String token = token_header.substring(7);
            User user = authController.requireLogin(token);
            if (!user.getRole().equals(Role.SELLER)) {
                sendErrorResponse(exchange, 403, "Forbidden request"); return;
            }
            Owner seller = (Owner) user;
            Restaurant seller_restaurant = seller.getRestaurant();
            if (seller_restaurant == null) {
                sendErrorResponse(exchange, 404, "Restaurant not found for this seller"); return;
            }
            RestaurantDto.RegisterReponseRestaurantDto restaurantDto = new RestaurantDto.RegisterReponseRestaurantDto(
                    seller_restaurant.getId(), seller_restaurant.getTitle(), seller_restaurant.getAddress(),
                    seller_restaurant.getPhone_number(), seller_restaurant.getLogoBase64(),
                    seller_restaurant.getTax_fee(), seller_restaurant.getAdditional_fee()
            );
            sendResponse(exchange, 200, gson.toJson(restaurantDto), "application/json");
        } catch (IOException e) {
            System.err.println("IOException in getSellerRestaurantAction: " + e.getMessage()); e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in getSellerRestaurantAction: " + e.getMessage()); e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: ");
        }
    }

    private void updateRestaurantAction(HttpExchange exchange, String restaurantId) {
        try {checkMediaType(exchange);
            String token_header = exchange.getRequestHeaders().getFirst("Authorization");
            if (token_header == null || !token_header.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); return;
            }
            String token = token_header.substring(7);
            User user = authController.requireLogin(token);
            if (!user.getRole().equals(Role.SELLER)) {
                sendErrorResponse(exchange, 403, "Forbidden request"); return;
            }
            if (((Owner)user).getRestaurant() == null) {
                sendErrorResponse(exchange,404,"Conflict occurred: Seller does not have a restaurant"); return;
            }
//            if (((Owner)user).getRestaurant().getStatus().equals(RestaurantStatus.WAITING)) {
//                sendErrorResponse(exchange,409,"Conflict occurred: restaurant is still unregistered"); return;
//            }
            String jsonBody = readRequestBody(exchange);
            if (jsonBody == null)
                new InvalidInputException(400, "body is null");
            RestaurantDto.RegisterRestaurantDto restaurantDto = objectMapper.readValue(jsonBody, RestaurantDto.RegisterRestaurantDto.class);
            RestaurantDto.RegisterReponseRestaurantDto response= restaurantController.editRestaurant(restaurantDto,(Owner)user);
            sendResponse(exchange, 200, gson.toJson(response), "application/json");
        } catch (JsonSyntaxException | com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage()); e.printStackTrace();
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException in createRestaurantAction: " + e.getMessage()); e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurantAction: " + e.getMessage()); e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void addRestaurantItemAction(HttpExchange exchange, String restaurantId) {
        try {
            System.out.println("Action: Add item to restaurant ID: " + restaurantId);
            // TODO: Implement logic for POST /restaurants/{id}/item
            // - Auth (ensure user is owner of restaurantId)
            // - checkMediaType(exchange);
            // - String jsonBody = readRequestBody(exchange);
            // - RestaurantDto.AddItemDto dto = objectMapper.readValue(jsonBody, ...);
            // - restaurantController.addItemToRestaurant(Long.parseLong(restaurantId), dto, (Owner) user);
            sendResponse(exchange, 201, gson.toJson(Map.of("message", "Item added to restaurant " + restaurantId + " (placeholder)")), "application/json");
        } catch (Exception e) { }
    }

    private void updateRestaurantItemAction(HttpExchange exchange, String restaurantId, String itemId) {
        try {
            System.out.println("Action: Update item " + itemId + " for restaurant " + restaurantId);
            // TODO: Implement logic for PUT /restaurants/{id}/item/{item_id}
            // - Auth (owner)
            // - checkMediaType(exchange);
            // - String jsonBody = readRequestBody(exchange);
            // - RestaurantDto.UpdateItemDto dto = objectMapper.readValue(jsonBody, ...);
            // - restaurantController.updateItemInRestaurant(Long.parseLong(restaurantId), Long.parseLong(itemId), dto, (Owner) user);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Item " + itemId + " in restaurant " + restaurantId + " updated (placeholder)")), "application/json");
        } catch (Exception e) { }
    }

    private void deleteRestaurantItemAction(HttpExchange exchange, String restaurantId, String itemId) {
        try {
            System.out.println("Action: Delete item " + itemId + " for restaurant " + restaurantId);
            // TODO: Implement logic for DELETE /restaurants/{id}/item/{item_id}
            // - Auth (owner)
            // - restaurantController.deleteItemFromRestaurant(Long.parseLong(restaurantId), Long.parseLong(itemId), (Owner) user);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Item " + itemId + " in restaurant " + restaurantId + " deleted (placeholder)")), "application/json"); // Or 204 No Content
        } catch (Exception e) {  }
    }

    private void addRestaurantMenuAction(HttpExchange exchange, String restaurantId) {
        try {
            System.out.println("Action: Add menu to restaurant ID: " + restaurantId);
            // TODO: Implement logic for POST /restaurants/{id}/menu
            // - Auth (owner)
            // - checkMediaType(exchange);
            // - String jsonBody = readRequestBody(exchange); // e.g., { "title": "Lunch Menu" }
            // - RestaurantDto.AddMenuDto dto = objectMapper.readValue(jsonBody, ...);
            // - restaurantController.addMenuToRestaurant(Long.parseLong(restaurantId), dto, (Owner) user);
            sendResponse(exchange, 201, gson.toJson(Map.of("message", "Menu added to restaurant " + restaurantId + " (placeholder)")), "application/json");
        } catch (Exception e) { }
    }

    private void deleteRestaurantMenuAction(HttpExchange exchange, String restaurantId, String title) {
        try {
            System.out.println("Action: Delete menu '" + title + "' for restaurant " + restaurantId);
            // TODO: Implement logic for DELETE /restaurants/{id}/menu/{title}
            // - Auth (owner)
            // - restaurantController.deleteMenuFromRestaurant(Long.parseLong(restaurantId), title, (Owner) user);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Menu '" + title + "' for restaurant " + restaurantId + " deleted (placeholder)")), "application/json"); // Or 204
        } catch (Exception e) { }
    }

    private void addItemToRestaurantMenuAction(HttpExchange exchange, String restaurantId, String title) {
        try {
            System.out.println("Action: Add item to menu '" + title + "' for restaurant " + restaurantId);
            // TODO: Implement logic for PUT /restaurants/{id}/menu/{title}
            // - Auth (owner)
            // - checkMediaType(exchange);
            // - String jsonBody = readRequestBody(exchange); // e.g., { "item_id": 123 }
            // - RestaurantDto.AddItemToMenuDto dto = objectMapper.readValue(jsonBody, ...);
            // - restaurantController.addItemToMenu(Long.parseLong(restaurantId), title, dto.getItemId(), (Owner) user);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Item added to menu '" + title + "' in restaurant " + restaurantId + " (placeholder)")), "application/json");
        } catch (Exception e) { }
    }

    private void removeItemFromRestaurantMenuAction(HttpExchange exchange, String restaurantId, String title, String itemId) {
        try {
            System.out.println("Action: Remove item " + itemId + " from menu '" + title + "' for restaurant " + restaurantId);
            // TODO: Implement logic for DELETE /restaurants/{id}/menu/{title}/{item_id}
            // - Auth (owner)
            // - restaurantController.removeItemFromMenu(Long.parseLong(restaurantId), title, Long.parseLong(itemId), (Owner) user);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Item " + itemId + " from menu '" + title + "' in restaurant " + restaurantId + " removed (placeholder)")), "application/json"); // Or 204
        } catch (Exception e) {  }
    }

    private void listRestaurantOrdersAction(HttpExchange exchange, String restaurantId) {
        try {
            System.out.println("Action: List orders for restaurant ID: " + restaurantId);
            String query = exchange.getRequestURI().getQuery(); // Handle query params like status, search, user, courier
            System.out.println("Query params: " + (query != null ? query : "none"));
            // TODO: Implement logic for GET /restaurants/{id}/orders
            // - Auth (owner or specific roles)
            // - Parse query parameters for filtering
            // - List<Order> orders = restaurantController.getOrdersForRestaurant(Long.parseLong(restaurantId), queryParamsMap, (Owner) user);
            // - Map to DTO list
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "List of orders for restaurant " + restaurantId + " (placeholder)")), "application/json");
        } catch (Exception e) { }
    }

    private void patchRestaurantOrderAction(HttpExchange exchange, String orderId) {
        try {
            System.out.println("Action: Patch order ID: " + orderId);
            // TODO: Implement logic for PATCH /restaurants/orders/{order_id}
            // - Auth (owner or specific roles for the restaurant this order belongs to - might need to fetch order first)
            // - checkMediaType(exchange);
            // - String jsonBody = readRequestBody(exchange); // e.g., { "status": "accepted" }
            // - RestaurantDto.PatchOrderDto dto = objectMapper.readValue(jsonBody, ...);
            // - restaurantController.patchOrderStatus(Long.parseLong(orderId), dto.getStatus(), user);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Order " + orderId + " patched (placeholder)")), "application/json");
        } catch (Exception e) { }
    }

    // --- Helper Methods ---
    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), java.nio.charset.StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading request body: " + e.getMessage());
            throw e;
        }
        return jsonBody.toString();
    }

    private void checkMediaType(HttpExchange exchange) throws InvalidInputException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {
            throw new InvalidInputException(415, "Unsupported Media Type");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] responseBytes = responseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage)  {

        // To ensure the errorMessage itself is a valid JSON string value if it contains quotes
        try {
            String errorMsgJson = gson.toJson(Map.of("error", errorMessage));
            sendResponse(exchange, statusCode, errorMsgJson, "application/json");
        }catch (Exception e) { e.printStackTrace(); }

    }

}