package observers;

import model.User;

public interface LoginObserver {
    void onUserLoggedIn(User user);
}

