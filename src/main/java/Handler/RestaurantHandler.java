package Handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import Controller.RestaurantController;
import exception.InvalidInputException;
import model.Restaurant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class RestaurantHandler implements HttpHandler {
    private final RestaurantController restaurantController=new RestaurantController();
    private final Gson gson=new Gson();

    @Override
    public void handle (HttpExchange exchange ) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            sendErrorResponse(exchange, 415, "Unsupported Media Type");
        }

        if (method.equals("POST") && path.equals("/restaurants")) {
            createRestaurant(exchange);
        }

    }
    private void createRestaurant(HttpExchange exchange) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        } catch (IOException e) {
            sendErrorResponse(exchange, 500, "Error reading request body: " + e.getMessage());
            return;
        }
        String json = jsonBody.toString();
        System.out.println("Received JSON: " + json);
        String response = "";
        Restaurant restaurant = null;
        try{
            restaurant= gson.fromJson(json, Restaurant.class);
            response=restaurantController.createRestaurant(restaurant);
            System.out.println("Created restaurant " + restaurant.getTitle());
            sendResponse(exchange,201,response,"application/json");

        }
        catch (JsonSyntaxException e)
        {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace(); // THIS IS IMPORTANT: Print the stack trace!
            sendErrorResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        }catch (InvalidInputException e) {
            sendErrorResponse(exchange,e.getStatus_code(), e.getMessage());
            System.out.println("Restaurant " + restaurant.getTitle() + " is invalid");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, e.getMessage());
        }
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
