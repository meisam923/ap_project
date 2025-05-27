package Handler;

import Controller.AuthController;
// No direct UserService import needed here if AuthController handles its dependencies
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.UserDto; // Your DTOs are nested here
import enums.Role;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Optional;

public class AuthHandler implements HttpHandler {
    private final AuthController authController;
    private final ObjectMapper objectMapper;

    public AuthHandler() {
        this.authController = AuthController.getInstance();
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
            if (method.equals("POST") || method.equals("PUT")) {
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {
                    sendErrorResponse(exchange, 415, new UserDto.ErrorResponseDTO("Unsupported Media Type: application/json required"));
                    return;
                }
            }

            if (method.equals("POST") && path.equals("/auth/register")) {
                handleRegistration(exchange);
                return;
            } else if (method.equals("POST") && path.equals("/auth/login")) {
                handleLogin(exchange);
                return;
            } else if (method.equals("GET") && path.equals("/auth/profile")) {
                handleGetProfile(exchange);
                return;
            } else if (method.equals("PUT") && path.equals("/auth/profile")) {
                handlePutProfile(exchange);
                return;
            } else if (method.equals("POST") && path.equals("/auth/logout")) {
                handlePostLogout(exchange);
                return;
            } else if (method.equals("POST") && path.equals("/auth/delete")) {
                handlePostDeleteAccount(exchange);
                return;
            } else if (method.equals("POST") && path.equals("/auth/refresh-token")) {
                handleTokenRefresh(exchange);
                return;
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource not found"));
            }
        } catch (Exception e) {
            System.err.println("Unhandled error in AuthHandler.handle() top level: " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
            }
        }
    }

    private void handleTokenRefresh(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null || (!exchange.getRequestMethod().equalsIgnoreCase("GET") && body.isEmpty())) {
            if (body != null && body.isEmpty()) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Request body is empty."));
            }
            return;
        }

        try {
            UserDto.RefreshTokenRequestDTO requestDto = objectMapper.readValue(body, UserDto.RefreshTokenRequestDTO.class);

            if (requestDto.refreshToken() == null || requestDto.refreshToken().isBlank()) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: refresh_token is missing."));
                return;
            }

            Optional<AuthController.LoginResponsePayload> refreshResult =
                    authController.refreshAccessToken(requestDto.refreshToken());

            if (refreshResult.isPresent()) {
                sendResponse(exchange, 200, refreshResult.get());
            } else {
                sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Failed to refresh token."));
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format in request body: " + e.getOriginalMessage()));
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error during POST /auth/refresh-token: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during token refresh."));
        }
    }


    private void handlePostDeleteAccount(HttpExchange exchange) throws IOException {
        try {
            String token = getJwtToken(exchange);
            if (token == null) {
                return;
            }

            boolean isDeleted = authController.deleteAccount(token);
            if (isDeleted) {
                sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Account deleted successfully"));
            } else {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Failed to delete account after authentication."));
            }
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error during POST /auth/delete: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during account deletion."));
        }
    }

    private void handlePostLogout(HttpExchange exchange) throws IOException {
        try {
            String token = getJwtToken(exchange);
            if (token == null) {
                return;
            }

            authController.logoutUser(token);

            sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Logout successful."));

        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error during POST /auth/logout: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during logout."));
        }
    }

    private void handlePutProfile(HttpExchange exchange) throws IOException {
        String token;
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
                sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Missing or malformed Bearer token."));
                return;
            }
            token = authHeader.substring(7);
            authController.requireLogin(token);

        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
            return;
        } catch (Exception e) {
            System.err.println("Error processing token for PUT /auth/profile: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during authentication."));
            return;
        }

        String body = readRequestBody(exchange);
        if (body == null || body.isEmpty() && !exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            if (body != null && body.isEmpty()) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Request body is empty."));
            }
            return;
        }

        try {
            UserDto.UpdateProfileRequestDTO requestDto = objectMapper.readValue(body, UserDto.UpdateProfileRequestDTO.class);

            String bankName = (requestDto.bankInfo() != null) ? requestDto.bankInfo().bankName() : null;
            String accountNumber = (requestDto.bankInfo() != null) ? requestDto.bankInfo().accountNumber() : null;

            boolean isChanged = authController.editProfile(
                    token,
                    requestDto.fullName(),
                    requestDto.phone(),
                    requestDto.email(),
                    requestDto.address(),
                    requestDto.profileImageBase64(),
                    bankName,
                    accountNumber,
                    null
            );

            if (isChanged) {
                sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Profile updated successfully"));
            } else {
                sendResponse(exchange, 200, new UserDto.MessageResponseDTO("Profile update processed. No changes were made or all values were the same."));
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format in request body: " + e.getOriginalMessage()));
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: " + e.getMessage()));
        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error during PUT /auth/profile: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error updating profile."));
        }
    }

    private void handleRegistration(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null || body.isEmpty() && !exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            if (body != null && body.isEmpty()) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Request body is empty."));
            }
            return;
        }

        try {
            UserDto.RegisterRequestDTO requestDto = objectMapper.readValue(body, UserDto.RegisterRequestDTO.class);

            if (requestDto.fullName() == null || requestDto.phone() == null || requestDto.password() == null ||
                    requestDto.role() == null || requestDto.address() == null) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Missing required fields."));
                return;
            }

            Role roleEnum;
            try {
                roleEnum = Role.valueOf(requestDto.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Invalid role value."));
                return;
            }

            Optional<User> registeredUserOpt = authController.register(
                    roleEnum,
                    requestDto.fullName(),
                    requestDto.phone(),
                    requestDto.email(),
                    requestDto.password(),
                    requestDto.address(),
                    requestDto.profileImageBase64(),
                    requestDto.bankInfo() != null ? requestDto.bankInfo().bankName() : null,
                    requestDto.bankInfo() != null ? requestDto.bankInfo().accountNumber() : null
            );

            if (registeredUserOpt.isPresent()) {
                User registeredUser = registeredUserOpt.get();
                Optional<AuthController.LoginResponsePayload> loginPayload = authController.login(registeredUser.getEmail(), requestDto.password(), true);

                if (loginPayload.isPresent()) {
                    UserDto.RegisterResponseDTO responseDto = new UserDto.RegisterResponseDTO(
                            "User registered successfully",
                            registeredUser.getPublicId(),
                            loginPayload.get().accessToken(),
                            loginPayload.get().refreshToken()
                    );
                    sendResponse(exchange, 200, responseDto);
                } else {
                    sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Registration succeeded, but token generation failed."));
                }
            } else {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Registration failed (internal error in controller)."));
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("email already exists") || e.getMessage().toLowerCase().contains("phone already exists"))) {
                sendErrorResponse(exchange, 409, new UserDto.ErrorResponseDTO(e.getMessage()));
            } else {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: " + e.getMessage()));
            }
        } catch (IOException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format in request body."));
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during registration."));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null || body.isEmpty() && !exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            if (body != null && body.isEmpty()) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Request body is empty."));
            }
            return;
        }

        try {
            UserDto.LoginRequestDTO requestDto = objectMapper.readValue(body, UserDto.LoginRequestDTO.class);

            if (requestDto.phone() == null || requestDto.password() == null) {
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: Phone and password are required."));
                return;
            }

            Optional<AuthController.LoginResponsePayload> loginPayloadOpt = authController.login(
                    requestDto.phone(),
                    requestDto.password(),
                    false
            );

            if (loginPayloadOpt.isPresent()) {
                AuthController.LoginResponsePayload payload = loginPayloadOpt.get();
                User loggedInUser = payload.user();

                UserDto.RegisterRequestDTO.BankInfoDTO bankInfoForSchema = null;
                if (loggedInUser.getBankName() != null && loggedInUser.getAccountNumber() != null) {
                    bankInfoForSchema = new UserDto.RegisterRequestDTO.BankInfoDTO(loggedInUser.getBankName(), loggedInUser.getAccountNumber());
                }

                UserDto.UserSchemaDTO userSchema = new UserDto.UserSchemaDTO(
                        loggedInUser.getPublicId(),
                        loggedInUser.getFullName(),
                        loggedInUser.getPhoneNumber(),
                        loggedInUser.getEmail(),
                        loggedInUser.getRole().name().toLowerCase(),
                        loggedInUser.getAddress(),
                        loggedInUser.getProfileImageBase64(),
                        bankInfoForSchema
                );

                UserDto.LoginResponseDTO responseDto = new UserDto.LoginResponseDTO(
                        "User logged in successfully",
                        payload.accessToken(),
                        payload.refreshToken(),
                        userSchema
                );
                sendResponse(exchange, 200, responseDto);
            } else {
                sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Invalid credentials."));
            }

        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (IOException e) {
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format in request body."));
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during login."));
        }
    }

    // handleGetProfile method
    private void handleGetProfile(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
                sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Missing or malformed Bearer token."));
                return;
            }
            String token = authHeader.substring(7);

            User user = authController.requireLogin(token);

            UserDto.RegisterRequestDTO.BankInfoDTO bankInfoForSchema = null;
            if (user.getBankName() != null && user.getAccountNumber() != null) {
                bankInfoForSchema = new UserDto.RegisterRequestDTO.BankInfoDTO(
                        user.getBankName(),
                        user.getAccountNumber()
                );
            }

            UserDto.UserSchemaDTO userSchema = new UserDto.UserSchemaDTO(
                    user.getPublicId(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole().name().toLowerCase(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    bankInfoForSchema
            );
            sendResponse(exchange, 200, userSchema);

        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error during GET /auth/profile: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error while retrieving profile."));
        }
    }


    private String readRequestBody(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET") || exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            return "";
        }
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading request body: " + e.getMessage());
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Error reading request body."));
            return null;
        }
        return jsonBody.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(responseObject);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to serialize response object: " + (responseObject != null ? responseObject.getClass().getName() : "null") + " - " + e.getMessage());
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                String fallbackErrorJson = "{ \"error\": \"Internal Server Error: Failed to generate response.\" }";
                exchange.sendResponseHeaders(500, fallbackErrorJson.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fallbackErrorJson.getBytes());
                } catch (IOException ex) {
                    System.err.println("CRITICAL: Failed to write fallback error response body: " + ex.getMessage());
                }
            }
            return;
        }

        if (exchange.getResponseCode() == -1) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        } else {
            System.err.println("WARNING: Headers already sent (current: " + exchange.getResponseCode() + "), cannot set new status " + statusCode + " for this response.");
        }
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) throws IOException {
        if (errorDto == null) {
            errorDto = new UserDto.ErrorResponseDTO("An unexpected error occurred and error details could not be generated.");
        }
        if (exchange.getResponseCode() == -1) {
            sendResponse(exchange, statusCode, errorDto);
        } else {
            System.err.println("Attempted to send error (Status: " + statusCode + ", Msg: " + errorDto.error() + ") but response already committed with status: " + exchange.getResponseCode());
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
}