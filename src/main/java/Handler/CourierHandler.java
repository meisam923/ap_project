package Handler;

import Controller.AuthController;
import Controller.CourierController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.RestaurantDto;
import enums.Role;
import exception.DeliveryAssigendException;
import exception.ForbiddenException;
import exception.InvalidInputException;
import exception.NotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import model.Deliveryman;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourierHandler implements HttpHandler {
    private final AuthController authController = AuthController.getInstance();
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CourierController courierController = new CourierController();

    private final List<CourierHandler.Route> routes = new ArrayList<>();

    record Route(String httpMethod, Pattern regexPattern, BiConsumer<HttpExchange, Matcher> action) {
    }
    public CourierHandler() {
        routes.add(new CourierHandler.Route("GET", Pattern.compile("^/deliveries/available$"), (exchange, matcher) ->
                getDeliveryRequestAction(exchange)
        ));

        routes.add(new CourierHandler.Route("PATCH", Pattern.compile("^/deliveries/(?<orderid>\\d+)$"), (exchange, matcher) -> {
            String order_Id = matcher.group("orderid");
            chengeOrderStatusAction(exchange,order_Id);
        }));

        routes.add(new CourierHandler.Route("GET", Pattern.compile("^/deliveries/history$"), (exchange, matcher) ->
                getDeliveryHistoryAction(exchange)
        ));
    }
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        CourierHandler.Route matchedRoute = null;
        Matcher pathMatcher = null;

        for (CourierHandler.Route route : routes) {
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
                sendErrorResponse(exchange, 404, "Resource Not Found");
        }
    }


    private void getDeliveryRequestAction(HttpExchange exchange) {
        System.out.println("getDeliveryHistoryAction");
        try{
        User user=getUserFromToken(exchange);
        if (!user.getRole().equals(Role.COURIER)) {
            sendErrorResponse(exchange, 403, "Forbidden request");
            return;
        }
        if (!user.isVerified()){
            throw new ForbiddenException(403);
        }
            List<RestaurantDto.OrderResponseDto> response=courierController.getAvailableDeliveryRequest();
            sendResponse(exchange,200,gson.toJson(response),"application/json");
        }
        catch (ForbiddenException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 403, "Forbidden request");
        }
        catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 401, "Unauthorized request");
        }
        catch (Exception e){
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    private void getDeliveryHistoryAction(HttpExchange exchange) {
    }

    private void chengeOrderStatusAction(HttpExchange exchange, String order_id) {
        try {
            User user=getUserFromToken(exchange);
            if (!user.getRole().equals(Role.COURIER)) {
                throw new ForbiddenException(403);
            }
            if (!user.isVerified()){
                throw new ForbiddenException(403);
            }
            checkMediaType(exchange);
            int orderId = extractInteger(order_id);
            String status = objectMapper.readTree(readRequestBody(exchange)).get("status").asText();
            RestaurantDto.OrderResponseDto response= courierController.changeOrderStatus((Deliveryman)  user, orderId, status);
            HashMap<String , Object> map = new HashMap<>();
            map.put("message", "Changed status successfully");
            map.put("order", response);
            sendResponse(exchange,200,gson.toJson(map),"application/json");

        }
        catch (DeliveryAssigendException e){
            e.printStackTrace();
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        }
        catch (NotFoundException e){
            e.printStackTrace();
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        }
        catch (InvalidInputException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, e.getStatusCode(), e.getMessage());
        }
        catch (ForbiddenException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, e.getStatus_code(), e.getMessage());
        }
        catch (AuthController.AuthenticationException | ExpiredJwtException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 401, "Unauthorized request");
        }
        catch (Exception e){
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    private int extractInteger(String str) throws InvalidInputException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(400, "Invalid id");
        }
    }

    private void checkMediaType(HttpExchange exchange) throws InvalidInputException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {
            throw new InvalidInputException(415, "Unsupported Media Type");
        }
    }

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
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] responseBytes = responseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
