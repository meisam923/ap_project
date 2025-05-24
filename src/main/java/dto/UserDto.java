package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private int id;
    private String fullName;
    private String phone;
    private String email;
    private String password;
    private String role;
    private String address;
    private String profileImageBase64;
    private BankInfo bankInfo;






    @Getter
    @Setter
    public static class BankInfo {
        private String bankName;
        private String accountNumber;
    }
}
