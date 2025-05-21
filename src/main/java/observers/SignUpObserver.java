package observers;

import model.User;

// define interface
public interface SignUpObserver {
    void onUserRegistered(User user);
}
