package Controller;

import Services.UserService;
import dao.RefreshTokenDao;
import dto.UserDto;
import enums.Role;
import model.*;
import observers.ForgetPasswordObserver;
import observers.LoginObserver;
import observers.SignUpObserver;
import org.jetbrains.annotations.NotNull;
import util.JwtUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class AuthController {

    private static AuthController instance;

    private final UserService userService = UserService.getInstance();
    private final RefreshTokenDao refreshTokenDao = new RefreshTokenDao();

    private final List<LoginObserver> loginObservers = new ArrayList<>();
    private final List<SignUpObserver> signUpObservers = new ArrayList<>();
    private final List<ForgetPasswordObserver> forgetPasswordObservers = new ArrayList<>();

    private final Map<String, PasswordResetTokenInfo> passwordResetTokens = new ConcurrentHashMap<>();
    private static final long RESET_CODE_VALIDITY_MS = 5 * 60 * 1000;

    private AuthController() {
    }

    public static synchronized AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }

    public void logoutUser(String accessToken) throws AuthenticationException {
        User user = requireLogin(accessToken);
        refreshTokenDao.deleteByUser(user);
    }

    public UserDto.LoginResponseDTO login(UserDto.LoginRequestDTO loginDto) throws Exception {
        long REFRESH_TOKEN_VALIDITY_MS = 1000L * 60 * 60 * 24 * 7;

        if ("admin".equals(loginDto.phone())) {
            if ("admin".equals(loginDto.password())) {
                System.out.println("Admin login successful.");
                User adminUser = new Customer("Admin", "Admin Address", "admin", "admin@app.com", "admin", null, null, null);
                adminUser.setRole(Role.ADMIN);

                String accessToken = JwtUtil.generateToken(adminUser);
                String refreshToken = JwtUtil.generateRefreshToken(adminUser, REFRESH_TOKEN_VALIDITY_MS);

                UserDto.UserSchemaDTO userDto = mapUserToSchemaDTO(adminUser);

                return new UserDto.LoginResponseDTO("Admin login successful", accessToken, refreshToken, userDto);
            } else {
                throw new AuthenticationException("Invalid credentials.");
            }
        }

        Optional<User> userOpt = userService.findByPhone(loginDto.phone());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid credentials.");
        }

        User user = userOpt.get();

        if (!loginDto.password().equals(user.getPassword())) {
            throw new AuthenticationException("Invalid credentials.");
        }

        String accessToken = JwtUtil.generateToken(user);
        String refreshToken = JwtUtil.generateRefreshToken(user, REFRESH_TOKEN_VALIDITY_MS);
        UserDto.UserSchemaDTO userDto = mapUserToSchemaDTO(user);

        return new UserDto.LoginResponseDTO("Login successful", accessToken, refreshToken, userDto);
    }

    private String generateAndStoreRefreshToken(@NotNull User user) {
        long refreshTokenValidityMs = 7 * 24 * 60 * 60 * 1000L;
        String refreshTokenStr = JwtUtil.generateRefreshToken(user, refreshTokenValidityMs);

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenStr,
                user,
                LocalDateTime.now().plusNanos(refreshTokenValidityMs * 1_000_000L)
        );
        refreshTokenDao.deleteByUser(user);
        refreshTokenDao.save(refreshToken);
        return refreshTokenStr;
    }

    public Optional<LoginResponsePayload> refreshAccessToken(String providedRefreshToken) {
        if (providedRefreshToken == null || providedRefreshToken.isBlank()) {
            throw new AuthenticationException("Refresh token is missing.");
        }

        Optional<RefreshToken> tokenOpt = refreshTokenDao.findByTokenString(providedRefreshToken);
        if (tokenOpt.isEmpty()) {
            throw new AuthenticationException("Refresh token not found or invalid.");
        }

        RefreshToken refreshToken = tokenOpt.get();
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenDao.delete(refreshToken);
            throw new AuthenticationException("Refresh token expired.");
        }

        User user = refreshToken.getUser();
        if (user == null) {
            refreshTokenDao.delete(refreshToken);
            throw new AuthenticationException("User associated with refresh token not found.");
        }

        String newAccessToken = JwtUtil.generateToken(user);
        return Optional.of(new LoginResponsePayload(newAccessToken, providedRefreshToken, user));
    }

    public boolean isTokenExpired(String token) {
        return JwtUtil.isTokenExpired(token);
    }

    public Optional<User> register(Role role, String fullName, String phoneNumber, String email, String password,
                                   String address, String profileImageBase64,
                                   String bankName, String accountNumber) {
        if (email != null && !email.isBlank() && userService.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && userService.findByPhone(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists: " + phoneNumber);
        }

        User user;
        try {
            user = UserService.UserFactory.createUser(role, fullName, phoneNumber, email, password,
                    bankName, accountNumber, address, profileImageBase64);
        } catch (IllegalArgumentException e) {
            throw e;
        }

        Optional<User> savedUserOpt = userService.addUser(user);
        if (savedUserOpt.isEmpty()) {
            throw new RuntimeException("User registration failed during save operation for email: " + email);
        }

        User savedUser = savedUserOpt.get();
        for (SignUpObserver obs : signUpObservers) {
            obs.onUserRegistered(savedUser);
        }
        return Optional.of(savedUser);
    }

    public String initiatePasswordReset(String email) throws UserNotFoundException {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Email not found: " + email));

        String code = String.valueOf(new Random().nextInt(100000, 999999));
        LocalDateTime expiryTime = LocalDateTime.now().plusNanos(RESET_CODE_VALIDITY_MS * 1_000_000L);
        passwordResetTokens.put(email, new PasswordResetTokenInfo(code, expiryTime, user.getPublicId()));

        for (ForgetPasswordObserver obs : forgetPasswordObservers) {
            obs.onForgetPassword(user, Integer.parseInt(code));
        }
        return code;
    }

    public boolean completePasswordReset(String email, String code, String newPassword) throws AuthenticationException, UserNotFoundException {
        PasswordResetTokenInfo tokenInfo = passwordResetTokens.get(email);

        if (tokenInfo == null || !tokenInfo.code().equals(code)) {
            throw new AuthenticationException("Invalid or non-existent password reset code.");
        }
        if (LocalDateTime.now().isAfter(tokenInfo.expiryTime())) {
            passwordResetTokens.remove(email);
            throw new AuthenticationException("Password reset code has expired.");
        }

        User user = userService.findByPublicId(tokenInfo.userPublicId())
                .orElseThrow(() -> {
                    passwordResetTokens.remove(email);
                    return new UserNotFoundException("User not found for password reset after code verification.");
                });

        boolean success = userService.resetPassword(user, newPassword);

        if (success) {
            passwordResetTokens.remove(email);
        }
        return success;
    }

    public boolean deleteAccount(String accessToken) throws AuthenticationException {
        User user = requireLogin(accessToken);
        refreshTokenDao.deleteByUser(user);
        return userService.removeUser(user);
    }

    public boolean editProfile(String accessToken, String fullName, String phoneNumber, String email,
                               String address, String profileImageBase64, String bankName, String accountNumber, Location location) throws AuthenticationException {
        User user = requireLogin(accessToken);

        boolean basicUpdated = userService.updateBasicProfile(user, fullName, phoneNumber, email, profileImageBase64, bankName, accountNumber);
        boolean specificUpdated = false;

        switch (user.getRole()) {
            case BUYER:
                if (user instanceof Customer customer) {
                    specificUpdated = userService.updateCustomerDetails(customer, address, location);
                }
                break;
            case SELLER:
                if (user instanceof Owner owner) {
                    specificUpdated = userService.updateOwnerDetails(owner, address, location);
                }
                break;
            case COURIER:
                if (user instanceof Deliveryman deliveryman) {
                    specificUpdated = userService.updateDeliveryLocation(deliveryman, location);
                }
                break;
        }
        return basicUpdated || specificUpdated;
    }

    public @NotNull User requireLogin(String token) throws AuthenticationException {
        try {
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Authentication token not provided.");
            }
            UserPayload payload = JwtUtil.verifyToken(token);
            if (payload == null) {
                throw new AuthenticationException("Invalid or expired token payload.");
            }

            return userService.findByPublicId(payload.getPublicId())
                    .orElseThrow(() -> new AuthenticationException("User not found for token, or token has been invalidated."));
        }catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException("Invalid token provided.");
        }
    }

    public void registerLoginObserver(LoginObserver obs) {
        if (obs != null && !loginObservers.contains(obs)) loginObservers.add(obs);
    }

    public void registerSignUpObserver(SignUpObserver obs) {
        if (obs != null && !signUpObservers.contains(obs)) signUpObservers.add(obs);
    }

    public void registerForgetPasswordObserver(ForgetPasswordObserver obs) {
        if (obs != null && !forgetPasswordObservers.contains(obs)) forgetPasswordObservers.add(obs);
    }

    public record LoginResponsePayload(String accessToken, String refreshToken, User user) {
    }

    private record PasswordResetTokenInfo(String code, LocalDateTime expiryTime, String userPublicId) {
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
    private UserDto.UserSchemaDTO mapUserToSchemaDTO(User user) {
        if (user == null) return null;

        UserDto.RegisterRequestDTO.BankInfoDTO bankInfo = null;
        if (user.getBankName() != null || user.getAccountNumber() != null) {
            bankInfo = new UserDto.RegisterRequestDTO.BankInfoDTO(user.getBankName(), user.getAccountNumber());
        }

        return new UserDto.UserSchemaDTO(
                user.getPublicId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole().name().toLowerCase(),
                user.getAddress(),
                user.getProfileImageBase64(),
                bankInfo
        );
    }
}