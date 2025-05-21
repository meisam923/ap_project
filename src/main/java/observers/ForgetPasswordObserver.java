package observers;

import model.User;

public interface ForgetPasswordObserver {
    void onForgetPassword(User user, int resetCode);
}
