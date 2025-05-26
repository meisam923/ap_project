package Handler;

import Controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import Controller.RestaurantController;
import dto.RestaurantDto;
import dto.UserDto;
import enums.Role;
import exception.InvalidInputException;
import model.Owner;
import model.Restaurant;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class RestaurantHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final RestaurantController restaurantController = new RestaurantController();
    private final Gson gson = new Gson();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
//        if (contentType == null || !contentType.equals("application/json")) {
//            sendErrorResponse(exchange, 415, "Unsupported Media Type");
//        }

        if (method.equals("POST") && path.equals("/restaurants")) {
            createRestaurant(exchange);
        }
        if (method.equals("GET") && path.equals("/restaurants/mine")) {
            getSellerRestaurant(exchange);
        }

    }

    private void createRestaurant(HttpExchange exchange) throws IOException {
        String token_header = exchange.getRequestHeaders().getFirst("Authorization");
        if (token_header == null || !token_header.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        }
        try{
        String token = token_header.substring(7);
        User user = authController.requireLogin(token);
        if (!user.getRole().equals(Role.SELLER)) {
            sendErrorResponse(exchange, 403, "Forbidden request");
        }
        if (((Owner)user).getRestaurant() != null) {
            sendErrorResponse(exchange,409,"Conflict occurred");
        }
        String jsonBody = readRequestBody(exchange);
        RestaurantDto.RegisterRestaurantDto restaurant = objectMapper.readValue(jsonBody, RestaurantDto.RegisterRestaurantDto.class);
        String json = jsonBody.toString();
        System.out.println("Received JSON: " + json);
            RestaurantDto.RegisterReponseRestaurantDto response = restaurantController.createRestaurant(restaurant, (Owner) user);
            System.out.println("Created restaurant " + restaurant.name());
            sendResponse(exchange, 201, gson.toJson(response), "application/json");
        } catch (JsonSyntaxException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace(); // THIS IS IMPORTANT: Print the stack trace!
            sendErrorResponse(exchange, 400, "Invalid input");
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void getSellerRestaurant(HttpExchange exchange) throws IOException {
        String token_header = exchange.getRequestHeaders().getFirst("Authorization");
        if (token_header == null || !token_header.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized request");
        }
        try{
            String token = token_header.substring(7);
            User user = authController.requireLogin(token);
            if (!user.getRole().equals(Role.SELLER)) {
                sendErrorResponse(exchange, 403, "Forbidden request");
            }
            Owner seller = (Owner) user;
            Restaurant seller_restaurant=seller.getRestaurant();
            RestaurantDto.RegisterReponseRestaurantDto restaurant=new RestaurantDto.RegisterReponseRestaurantDto(seller_restaurant.getId(),seller_restaurant.getTitle(),seller_restaurant.getAddress(),seller_restaurant.getPhone_number(),seller_restaurant.getLogoBase64(),seller_restaurant.getTax_fee(),seller_restaurant.getAdditional_fee());
            if (restaurant==null){
                sendErrorResponse(exchange, 400, "json/application");
            }
            sendResponse(exchange, 200, gson.toJson(restaurant), "application/json");
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Invalid input");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        } catch (IOException e) {
            sendErrorResponse(exchange, 500, "Error reading request body: " + e.getMessage());
            return null;
        }
        return jsonBody.toString();
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBody.getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        String jsonError = String.format("{ \"error\": \"%s\" }", errorMessage);
        sendResponse(exchange, statusCode, jsonError, "application/json");
    }
}
