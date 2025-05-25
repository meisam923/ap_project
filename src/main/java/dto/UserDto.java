package dto;

import exception.InvalidInputException;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private int user_id;
    private String token;
    private String full_name;
    private String phone;
    private String email;
    private String password;
    private String role;
    private String address;
    private String profileImageBase64;
    private BankInfo bank_info;

    public UserDto(){

    }

    @Getter
    @Setter
    public static class BankInfo {
        private String bank_name;
        private String account_number;
    }
    public String validateFields() {
        if (this.full_name == null || this.full_name.trim().equals("")) {
            return "full_name";
        }
        if (this.phone == null || !this.phone.startsWith("09") || this.phone.length() != 11) {
            return "phone";
        }
        if (this.email != null && !Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", email)) {
            return "email";
        }
        if (this.password == null || this.password.trim().equals("")) {
            return "password";
        }
        String tmp = this.role.trim();
        if (tmp == null || (!tmp.equals("buyer") && !tmp.equals("vendor") && !tmp.equals("admin") && !tmp.equals("courier"))) {
            return "role";
        }
        if (this.address == null || this.full_name.trim().equals("")) {
            return "address";
        }
        if (this.bank_info.getBank_name() == null || this.bank_info.getBank_name().trim().equals("")) {
            return "bank_name";
        }
        if (this.bank_info.getAccount_number() == null || this.bank_info.getAccount_number().trim().equals("")) {
            return "bank_name";
        } return null;
    }
}
