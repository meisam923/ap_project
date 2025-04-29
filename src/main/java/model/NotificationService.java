package model;

public class NotificationService implements SignUpObserver {
    public void onUserRegistered(User user) {  //happens after user signed up
        System.out.println("Welcome " + user.getFirstName() + "Notification sent");
    }
}

