package Handler;

import Controller.AuthController;
import Controller.CourierController;
import Controller.RestaurantController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import dto.RestaurantDto;
import enums.Role;
import exception.ForbiddenException;
import io.jsonwebtoken.ExpiredJwtException;
import model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourierHandler {
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


    private void getDeliveryHistoryAction(HttpExchange exchange) {
        User user=getUserFromToken(exchange);
        if (!user.getRole().equals(Role.COURIER)) {
            sendErrorResponse(exchange, 403, "Forbidden request");
            return;
        }
        try {
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
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void getDeliveryRequestAction(HttpExchange exchange) {
    }

    private void chengeOrderStatusAction(HttpExchange exchange, String order_id) {
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
