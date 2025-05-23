import Services.AuthService;
import enums.Role;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        AuthService auth = AuthService.getInstance();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("=== PolyEats Console ===");
            System.out.println("1) Register");
            System.out.println("2) Login");
            System.out.println("3) Request Password Reset");
            System.out.println("4) Delete Account");
            System.out.println("5) Edit Profile");
            System.out.println("0) Exit");
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> doRegister(auth, sc);
                case "2" -> doLogin(auth, sc);
                case "3" -> doPasswordReset(auth, sc);
                case "4" -> doDeleteAccount(auth, sc);
                case "5" -> doEditProfile(auth, sc);
                case "0" -> {
                    System.out.println("Goodbye!");
                    sc.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    private static void doRegister(AuthService auth, Scanner sc) {
        try {
            System.out.print("Role (CUSTOMER, OWNER, DELIVERY_MAN): ");
            Role role = Role.valueOf(sc.nextLine().trim().toUpperCase());

            System.out.print("First Name: ");
            String fn = sc.nextLine();

            System.out.print("Last Name: ");
            String ln = sc.nextLine();

            System.out.print("Phone: ");
            String phone = sc.nextLine();

            System.out.print("Email: ");
            String email = sc.nextLine();

            System.out.print("Password: ");
            String pwd = sc.nextLine();

            // For simplicity, no address/location/restaurant
            var user = auth.register(role, fn, ln, phone, email, pwd, null, null, null);
            if (user != null) {
                System.out.println("✅ Registered successfully. Please login.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void doLogin(AuthService auth, Scanner sc) {
        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Password: ");
        String pwd = sc.nextLine();

        String token = auth.login(email, pwd);
        if (token != null) {
            System.out.println("✅ Login successful.");
            System.out.println("Access Token: " + token);
        } else {
            System.out.println("❌ Login failed.");
        }
    }

    private static void doPasswordReset(AuthService auth, Scanner sc) {
        System.out.print("Email: ");
        String email = sc.nextLine();
        auth.requestPasswordReset(email);
        // AuthService runs its own prompt loop for code and new password
    }

    private static void doDeleteAccount(AuthService auth, Scanner sc) {
        System.out.print("Access Token: ");
        String token = sc.nextLine();
        auth.deleteAccount(token);
    }

    private static void doEditProfile(AuthService auth, Scanner sc) {
        System.out.print("Access Token: ");
        String token = sc.nextLine();

        System.out.print("New First Name (or blank to skip): ");
        String fn = sc.nextLine();
        System.out.print("New Last Name (or blank to skip): ");
        String ln = sc.nextLine();
        System.out.print("New Phone (or blank to skip): ");
        String phone = sc.nextLine();
        System.out.print("New Email (or blank to skip): ");
        String email = sc.nextLine();
        System.out.print("New Password (or blank to skip): ");
        String pwd = sc.nextLine();

        // If any input is blank, pass null so AuthService skips updating it
        auth.editProfile(
                token,
                fn.isBlank() ? null : fn,
                ln.isBlank() ? null : ln,
                phone.isBlank() ? null : phone,
                email.isBlank() ? null : email,
                pwd.isBlank() ? null : pwd,
                null,  // no address in console demo
                null   // no location in console demo
        );
    }
}
