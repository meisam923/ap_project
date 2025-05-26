package Handler;

import Controller.AuthController;
import Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.UserDto;
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
            } else if (method.equals("POST") && path.equals("/auth/login")) {
                handleLogin(exchange);
            } else if (method.equals("GET") && path.equals("/auth/profile")) {
                // TODO: Implement
                sendErrorResponse(exchange, 501, new UserDto.ErrorResponseDTO("Not Implemented: GET /auth/profile"));
            } else if (method.equals("PUT") && path.equals("/auth/profile")) {
                // TODO: Implement
                sendErrorResponse(exchange, 501, new UserDto.ErrorResponseDTO("Not Implemented: PUT /auth/profile"));
            } else if (method.equals("POST") && path.equals("/auth/logout")) {
                // TODO: Implement
                sendErrorResponse(exchange, 501, new UserDto.ErrorResponseDTO("Not Implemented: POST /auth/logout"));
            } else {
                sendErrorResponse(exchange, 404, new UserDto.ErrorResponseDTO("Resource not found"));
            }
        } catch (Exception e) {
            System.err.println("Unhandled error in AuthHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error"));
        }
    }

    private void handleRegistration(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null) return;

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
                sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid Input: value."));
                return;
            }

            Optional<User> registeredUserOpt = authController.register(
                    roleEnum,
                    requestDto.fullName(),
                    requestDto.phone(),
                    requestDto.email(),
                    requestDto.password(),
                    requestDto.address(),
                    null,
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
                            loginPayload.get().accessToken()
                    );
                    sendResponse(exchange, 200, responseDto);
                } else {
                    sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Registration succeeded, but token generation failed."));
                }
            } else {
                sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Registration failed (internal)."));
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("email already exists") || e.getMessage().toLowerCase().contains("phone already exists"))) {
                sendErrorResponse(exchange, 409, new UserDto.ErrorResponseDTO(e.getMessage())); // Conflict
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
        if (body == null) return;

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
                        userSchema
                );
                sendResponse(exchange, 200, responseDto);
            } else {
                sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: Invalid credentials."));
            }

        } catch (AuthController.AuthenticationException e) {
            sendErrorResponse(exchange, 401, new UserDto.ErrorResponseDTO("Unauthorized: " + e.getMessage()));
        } catch (IOException e) { // JSON parsing errors
            sendErrorResponse(exchange, 400, new UserDto.ErrorResponseDTO("Invalid JSON format in request body."));
        } catch (Exception e) { // Catch other exceptions
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Internal Server Error during login."));
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
            sendErrorResponse(exchange, 500, new UserDto.ErrorResponseDTO("Error reading request body."));
            return null;
        }
        return jsonBody.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(responseObject);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, UserDto.ErrorResponseDTO errorDto) throws IOException {
        if (errorDto == null) {
            errorDto = new UserDto.ErrorResponseDTO("An unexpected error occurred and error details could not be generated.");
        }
        sendResponse(exchange, statusCode, errorDto);
    }
}