package model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public static String login(User user) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);

        return token;
    }

    public static User getUserByToken(String token) {
        return activeSessions.get(token);
    }

    public static void logout(String token) {
        activeSessions.remove(token);
    }
    public static boolean isLoggedIn(User user) {
        return activeSessions.containsValue(user);
    }
    public static boolean isLoggedInById(String userId) { //IDK if we ll need this
        return activeSessions.values().stream()
                .anyMatch(u -> u.getPublicId().equals(userId));
    }
}

