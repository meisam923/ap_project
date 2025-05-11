package model;

public interface ForgetPasswordObserver {
    void onForgetPassword(User user, int resetCode);
}
