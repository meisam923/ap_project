package Handler;

import Controller.AuthController;
import Controller.RestaurantController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.RestaurantDto;
import enums.ApprovalStatus;
import enums.OperationalStatus;
import enums.Role;
import exception.ConflictException;
import exception.InvalidInputException;
import exception.NotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import model.Owner;
import model.Restaurant;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; // For placeholder responses
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestaurantHandler implements HttpHandler {
    // Controllers and helpers
    private final AuthController authController = AuthController.getInstance();
    private final RestaurantController restaurantController = new RestaurantController();
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final ObjectMapper objectMapper = new ObjectMapper();


    private final List<Route> routes = new ArrayList<>();

    record Route(String httpMethod, Pattern regexPattern, BiConsumer<HttpExchange, Matcher> action) {
    }

    public RestaurantHandler() {
        // === Routes

        // POST /restaurants
        routes.add(new Route("POST", Pattern.compile("^/restaurants$"), (exchange, matcher) -> createRestaurantAction(exchange)));

        // GET /restaurants/mine
        routes.add(new Route("GET", Pattern.compile("^/restaurants/mine$"), (exchange, matcher) -> getSellerRestaurantAction(exchange)));

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
                sendErrorResponse(exchange, 404, "Resource Not Found");
            } else {
                sendErrorResponse(exchange, 404, "Resource Not Found");
            }
        }
    }

    // --- Action Methods (Your existing + Placeholders for the specified routes) ---

    private void createRestaurantAction(HttpExchange exchange) {
        try {
            checkMediaType(exchange);
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant() != null) {
                sendErrorResponse(exchange, 409, "Conflict occurred: Seller already has a restaurant");
                return;
            }
            String jsonBody = readRequestBody(exchange);
            if (jsonBody == null) {
                sendErrorResponse(exchange, 400, "Body is empty");
                return;
            }

            RestaurantDto.RegisterRestaurantDto restaurantDto = objectMapper.readValue(jsonBody, RestaurantDto.RegisterRestaurantDto.class);
            RestaurantDto.RegisterReponseRestaurantDto response = restaurantController.createRestaurant(restaurantDto, (Owner) user);
            sendResponse(exchange, 201, gson.toJson(response), "application/json");
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (JsonSyntaxException | com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurantAction: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void getSellerRestaurantAction(HttpExchange exchange) {
        try {
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            Owner seller = (Owner) user;
            Restaurant seller_restaurant = seller.getRestaurant();
            if (seller_restaurant == null) {
                sendErrorResponse(exchange, 404, "Restaurant not found for this seller");
                return;
            }
            RestaurantDto.RegisterReponseRestaurantDto restaurantDto = new RestaurantDto.RegisterReponseRestaurantDto(seller_restaurant.getId(), seller_restaurant.getTitle(), seller_restaurant.getAddress(), seller_restaurant.getPhoneNumber(), seller_restaurant.getLogoBase64(), seller_restaurant.getTaxFee(), seller_restaurant.getAdditionalFee());
            List<RestaurantDto.RegisterReponseRestaurantDto> list = new ArrayList<>();
            list.add(restaurantDto);
            sendResponse(exchange, 200, gson.toJson(list), "application/json");
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            System.err.println("Unexpected error in getSellerRestaurantAction: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: ");
        }
    }

    private void updateRestaurantAction(HttpExchange exchange, String restaurantId) {
        try {
            checkMediaType(exchange);
            int restaurant_id = extractInteger(restaurantId);
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant() == null || ((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Conflict occurred: Seller does not have a restaurant");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            String jsonBody = readRequestBody(exchange);
            if (jsonBody == null) new InvalidInputException(400, "body is null");
            RestaurantDto.RegisterRestaurantDto restaurantDto = objectMapper.readValue(jsonBody, RestaurantDto.RegisterRestaurantDto.class);
            RestaurantDto.RegisterReponseRestaurantDto response = restaurantController.editRestaurant(restaurantDto, (Owner) user);
            sendResponse(exchange, 200, gson.toJson(response), "application/json");
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (JsonSyntaxException | com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurantAction: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    // TODO : conflict if a that item exist implemention status :false
    private void addRestaurantItemAction(HttpExchange exchange, String restaurantId) {
        try {
            checkMediaType(exchange);
            User user = getUserFromToken(exchange);
            if (user == null) return;

            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            String jsonBody = readRequestBody(exchange);
            if (jsonBody == null || jsonBody.isEmpty()) {
                sendErrorResponse(exchange, 400, "body is empty");
                return;
            }
            RestaurantDto.AddItemToRestaurantDto itemdto = objectMapper.readValue(jsonBody, RestaurantDto.AddItemToRestaurantDto.class);
            RestaurantDto.AddItemToRestaurantResponseDto response = restaurantController.addItemTORestaurant(itemdto, ((Owner) user).getRestaurant());

            String jsonResponse = objectMapper.writeValueAsString(response);
            sendResponse(exchange, 200, jsonResponse, "application/json");

        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (JsonProcessingException e) {
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in addRestaurantItemAction: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void updateRestaurantItemAction(HttpExchange exchange, String restaurantId, String itemId) {
        try {
            System.out.println("Action: Update item " + itemId + " for restaurant " + restaurantId);
            // TODO: Implement logic for PUT /restaurants/{id}/item/{item_id}
            checkMediaType(exchange);
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            int item_id = extractInteger(itemId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            String jsonBody = readRequestBody(exchange);
            if (jsonBody == null) {
                sendErrorResponse(exchange, 400, "body is empty");
                return;
            }
            RestaurantDto.AddItemToRestaurantDto itemdto = objectMapper.readValue(jsonBody, RestaurantDto.AddItemToRestaurantDto.class);
            RestaurantDto.AddItemToRestaurantResponseDto response = restaurantController.editItemTORestaurant(itemdto, ((Owner) user).getRestaurant(), item_id);
            sendResponse(exchange, 200, gson.toJson(response), "application/json");
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (JsonSyntaxException | com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurantAction: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void deleteRestaurantItemAction(HttpExchange exchange, String restaurantId, String itemId) {
        try {
            System.out.println("Action: Delete item " + itemId + " for restaurant " + restaurantId);
            // TODO: Implement logic for DELETE /restaurants/{id}/item/{item_id}
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            int item_id = extractInteger(itemId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            restaurantController.deleteItemfromRestaurant(((Owner) user).getRestaurant(), item_id);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Food item removed successfully")), "application/json");
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (JsonSyntaxException | com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "Invalid JSON input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurantAction: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void addRestaurantMenuAction(HttpExchange exchange, String restaurantId) {
        try {
            System.out.println("Action: Add menu to restaurant ID: " + restaurantId);


            checkMediaType(exchange);
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            String title = objectMapper.readTree(readRequestBody(exchange)).get("title").asText();
            if (title == null) {
                throw new InvalidInputException(400, "title");
            }
            restaurantController.addMenoToRestaurant(((Owner) user).getRestaurant(), title);
            sendResponse(exchange, 200, gson.toJson(Map.of("title", title)), "application/json");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (ConflictException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            System.err.println("Unexpected error in createRestaurat menu: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: ");
        }
    }

    private void deleteRestaurantMenuAction(HttpExchange exchange, String restaurantId, String title) {
        try {
            System.out.println("Action: Delete menu '" + title + "' for restaurant " + restaurantId);
            User user = getUserFromToken(exchange);
            if (title == null) {
                throw new InvalidInputException(400, "title");
            }
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            restaurantController.deleteMenoFromRestaurant(((Owner) user).getRestaurant(), title);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Food menu removed from restaurant successfully")), "application/json"); // Or 204
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: ");
        }
    }

    private void addItemToRestaurantMenuAction(HttpExchange exchange, String restaurantId, String title) {
        try {
            System.out.println("Action: Add item to menu '" + title + "' for restaurant " + restaurantId);
            // TODO: Implement logic for PUT /restaurants/{id}/menu/{title}
            checkMediaType(exchange);
            User user = getUserFromToken(exchange);
            if (title == null) {
                throw new InvalidInputException(400, "title");
            }
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant().getMenu(title) == null) {
                sendErrorResponse(exchange, 404, "Menu not found");
            }
            String itemId = objectMapper.readTree(readRequestBody(exchange)).get("item_id").asText();
            int item_id = extractInteger(itemId);
            restaurantController.addAItemToMenu(((Owner) user).getRestaurant(), title, item_id);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Food item add to menu successfully")), "application/json");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (ConflictException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: ");
        }
    }

    private void removeItemFromRestaurantMenuAction(HttpExchange exchange, String restaurantId, String title, String itemId) {
        try {
            System.out.println("Action: Remove item " + itemId + " from menu '" + title + "' for restaurant " + restaurantId);
            // TODO: Implement logic for DELETE /restaurants/{id}/menu/{title}/{item_id}
            User user = getUserFromToken(exchange);
            if (title == null) {
                throw new InvalidInputException(400, "title");
            }
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            if (((Owner) user).getRestaurant().getMenu(title) == null) {
                sendErrorResponse(exchange, 404, "Menu not found");
            }
            int item_id = extractInteger(itemId);
            restaurantController.deleteAItemFromMenu(((Owner) user).getRestaurant(), title, item_id);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Item removed from restaurant menu successfully")), "application/json");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "error in reading json");
        } catch (Exception e) {
        }
    }

    private void listRestaurantOrdersAction(HttpExchange exchange, String restaurantId) {
        try {
            System.out.println("Action: List orders for restaurant ID: " + restaurantId);
            String query = exchange.getRequestURI().getQuery(); // Handle query params like status, search, user, courier
            System.out.println("Query params: " + (query != null ? query : "none"));
            // TODO: Implement logic for GET /restaurants/{id}/orders
            HashMap<String, String> params = getQueryParmaters(query);
            System.out.println(params);
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int restaurant_id = extractInteger(restaurantId);
            if (((Owner) user).getRestaurant().getId() != restaurant_id) {
                sendErrorResponse(exchange, 404, "Resource not found");
                return;
            }
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            List<RestaurantDto.OrderResponseDto> response = restaurantController.getRestaurantOrders(params, restaurant_id);
            System.out.println(response);
            sendResponse(exchange, 200, gson.toJson(response), "application/json");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void patchRestaurantOrderAction(HttpExchange exchange, String orderId) {
        try {
            System.out.println("Action: Patch order ID: " + orderId);
            // TODO: Implement logic for PATCH /restaurants/orders/{order_id}
            checkMediaType(exchange);
            User user = getUserFromToken(exchange);
            if (!user.getRole().equals(Role.SELLER) || !user.isVerified()) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            int order_id = extractInteger(orderId);
            if (((Owner) user).getRestaurant().getApprovalStatus().equals(ApprovalStatus.WAITING)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
                return;
            }
            String status = objectMapper.readTree(readRequestBody(exchange)).get("status").asText();
            restaurantController.changeOrderStatus((Owner) user, status, order_id);
            sendResponse(exchange, 200, gson.toJson(Map.of("message", "Order status changed successfully")), "application/json");
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, 404, "Resource not found");
        } catch (ConflictException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }

    // --- Helper Methods ---
    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), java.nio.charset.StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
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

    private int extractInteger(String str) throws InvalidInputException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(400, "Invalid id");
        }
    }

    private HashMap<String, String> getQueryParmaters(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        HashMap<String, String> result = new HashMap<>();
        String[] queryArr = query.split("&");
        for (String queryParm : queryArr) {
            if (!queryParm.isEmpty() || !queryParm.contains("=")) {
                String[] keyVal = queryParm.split("=");
                if (keyVal.length == 2) {
                    result.put(keyVal[0], keyVal[1]);
                }
            }
        }
        return result;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] responseBytes = responseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private User getUserFromToken(HttpExchange exchange) throws AuthController.AuthenticationException {
        String token_header = exchange.getRequestHeaders().getFirst("Authorization");
        if (token_header == null || !token_header.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
            return null;
        }
        String token = token_header.substring(7);
        return authController.requireLogin(token);
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) {
        try {
            String errorMsgJson = gson.toJson(Map.of("error", errorMessage));
            sendResponse(exchange, statusCode, errorMsgJson, "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}