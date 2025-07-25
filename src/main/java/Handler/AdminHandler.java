package Handler;

import Controller.AdminController;
import Controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.*;
import exception.ForbiddenException;
import exception.InvalidInputException;
import exception.NotFoundException;
import model.Admin;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdminHandler implements HttpHandler {
    private final AdminController adminController = new AdminController();
    private final AuthController authController = AuthController.getInstance();
    private final ObjectMapper objectMapper;
    private final List<RestaurantHandler.Route> routes = new ArrayList<>();

    public AdminHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        routes.add(new RestaurantHandler.Route("GET", Pattern.compile("^/admin/users$"), (exchange, matcher) -> handleGetAllUsers(exchange)));
        routes.add(new RestaurantHandler.Route("GET", Pattern.compile("^/admin/restaurants$"), (exchange, matcher) -> handleGetAllRestaurants(exchange)));
        routes.add(new RestaurantHandler.Route("PATCH", Pattern.compile("^/admin/users/(?<id>\\d+)/status$"), (exchange, matcher) -> {
            String userId = matcher.group("id");
            handleUserStatus(exchange, userId);
        }));
        routes.add(new RestaurantHandler.Route("PATCH", Pattern.compile("^/admin/restaurants/(?<id>\\d+)/status$"), (exchange, matcher) -> {
            String userId = matcher.group("id");
            handleRestaurantStatus(exchange, userId);
        }));
        routes.add(new RestaurantHandler.Route("GET", Pattern.compile("^/admin/orders$"), (exchange, matcher) -> handleViewAllOrders(exchange)));
        routes.add(new RestaurantHandler.Route("GET", Pattern.compile("^/admin/transactions$"), (exchange, matcher) -> handleViewFinancialTransactions(exchange)));
        routes.add(new RestaurantHandler.Route("GET", Pattern.compile("^/admin/coupons$"), (exchange, matcher) -> handleGetAllCoupons(exchange)));
        routes.add(new RestaurantHandler.Route("POST", Pattern.compile("^/admin/coupons$"), (exchange, matcher) -> handleCreateCoupon(exchange)));
        routes.add(new RestaurantHandler.Route("DELETE", Pattern.compile("^/admin/coupons/(?<id>\\d+)$"), (exchange, matcher) ->
        {
            String id = matcher.group("id");
            handleDeleteCoupons(exchange, id);
        }));
        routes.add(new RestaurantHandler.Route("PUT", Pattern.compile("^/admin/coupons/(?<id>\\d+)$"), (exchange, matcher) -> {
            String id = matcher.group("id");
            handleUpdateCoupons(exchange, id);
        }));
        routes.add(new RestaurantHandler.Route("GET", Pattern.compile("^/admin/coupons/(?<id>\\d+)$"), (exchange, matcher) ->
        {
            String id = matcher.group("id");
            handleGetCouponsDetails(exchange, id);
        }));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        RestaurantHandler.Route matchedRoute = null;
        Matcher pathMatcher = null;

        for (RestaurantHandler.Route route : routes) {
            Matcher currentMatcher = route.regexPattern().matcher(requestPath);
            if (currentMatcher.matches()) {
                if (route.httpMethod().equalsIgnoreCase(requestMethod)) {
                    try {
                        Admin ad = getUserFromToken(exchange);
                    } catch (AuthController.AuthenticationException e) {
                        e.printStackTrace();
                        sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO(e.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));

                    }
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
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        } else {
            if (pathMatcher != null) {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource Not Found"));
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource Not Found"));
            }
        }
    }

    private void handleGetAllUsers(HttpExchange exchange) {
        try {
            sendResponse(exchange, 200, adminController.getAllUsers());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleUserStatus(HttpExchange exchange, String id) {
        try {
            System.out.println("User " + id + " is online");
            checkMediaType(exchange);
            String body = readRequestBody(exchange);
            AdminDto.UpdateUserStatusRequestDTO updateDto = objectMapper.readValue(body, AdminDto.UpdateUserStatusRequestDTO.class);
            if (updateDto.status() == null || updateDto.status().equals("")) {
                throw new InvalidInputException(400, "Status");
            }
            adminController.updateUserApprovalStatus(id, updateDto.status());
            sendResponse(exchange, 200, new UserDto.ErrorResponseDTO("Status updated"));
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatusCode(), new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (ForbiddenException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }
    private void handleRestaurantStatus(HttpExchange exchange, String id) {
        try {
            System.out.println("User " + id + " is online");
            checkMediaType(exchange);
            String body = readRequestBody(exchange);
            AdminDto.UpdateUserStatusRequestDTO updateDto = objectMapper.readValue(body, AdminDto.UpdateUserStatusRequestDTO.class);
            if (updateDto.status() == null || updateDto.status().equals("")) {
                throw new InvalidInputException(400, "Status");
            }
            adminController.updateRestaurantStatus(id, updateDto.status());
            sendResponse(exchange, 200, new UserDto.ErrorResponseDTO("Status updated"));
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatusCode(), new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (ForbiddenException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 403, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleViewAllOrders(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);
            String searchFilter = params.get("search");
            List<AdminDto.OrderSchemaDTO> response = adminController.getAllOrders(searchFilter);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }

    private void handleViewFinancialTransactions(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);
            String searchFilter = params.get("search");
            List<AdminDto.TransactionSchemaDTO> response = adminController.getAllTransactions(searchFilter);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }

    private void handleGetAllCoupons(HttpExchange exchange) {
        try {
            sendResponse(exchange, 200, adminController.getAllCoupons());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }

    }

    private void handleCreateCoupon(HttpExchange exchange) {
        try {
            checkMediaType(exchange);
            String body = readRequestBody(exchange);
            if (body == null || body.isEmpty()) {
                throw new InvalidInputException(400, "Body");
            }
            CouponDto.CouponInputSchemaDTO couponDto = objectMapper.readValue(body, CouponDto.CouponInputSchemaDTO.class);
            sendResponse(exchange, 200, adminController.createCoupon(couponDto));
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatusCode(), new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }

    private void handleDeleteCoupons(HttpExchange exchange, String id) {
        try {
            int ID = Integer.parseInt(id);
            adminController.deleteCoupon(ID);
            sendResponse(exchange, 200, new UserDto.ErrorResponseDTO("Coupon deleted"));
        } catch (NotFoundException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Coupon not found"));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }

    private void handleUpdateCoupons(HttpExchange exchange, String id) {
        try {
            checkMediaType(exchange);
            int ID = Integer.parseInt(id);
            String body = readRequestBody(exchange);
            if (body == null || body.isEmpty()) {
                throw new InvalidInputException(400, "Body");
            }
            CouponDto.CouponInputSchemaDTO couponDto = objectMapper.readValue(body, CouponDto.CouponInputSchemaDTO.class);
            sendResponse(exchange, 200, adminController.updateCoupon(couponDto, ID));
        } catch (InvalidInputException e) {
            sendErrorResponse(exchange, e.getStatusCode(), new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }

    private void handleGetCouponsDetails(HttpExchange exchange, String id) {
        try {
            int ID = Integer.parseInt(id);
            sendResponse(exchange, 200, adminController.getCouponDetails(ID));
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }
    public void handleGetAllRestaurants(HttpExchange exchange) {
        try{
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);
            String searchFilter = params.get("search");
            sendResponse(exchange,200,adminController.getAllRestaurants(searchFilter));

        } catch (Exception e){
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal server error"));
        }
    }

    private Admin getUserFromToken(HttpExchange exchange) throws Exception {
        String tokenHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (tokenHeader == null || !tokenHeader.toLowerCase().startsWith("bearer ")) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Missing or malformed Bearer token."));
            return null;
        }
        String token = tokenHeader.substring(7);

        return adminController.CheckAdminValidation(token);
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

    private void checkMediaType(HttpExchange exchange) throws InvalidInputException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {
            throw new InvalidInputException(415, "Unsupported Media Type");
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) jsonBody.append(line);
        }
        return jsonBody.toString();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1 && !pair[1].isEmpty()) params.put(pair[0], pair[1]);
        }
        return params;
    }
}