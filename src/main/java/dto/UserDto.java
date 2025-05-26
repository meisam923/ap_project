package dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class UserDto {
    public record RegisterRequestDTO(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("role") String role,
            @JsonProperty("address") String address,
            @JsonProperty("profileImageBase64") String profileImageBase64,
            @JsonProperty("bank_info") BankInfoDTO bankInfo
    ) {
        public record BankInfoDTO(
                @JsonProperty("bank_name") String bankName,
                @JsonProperty("account_number") String accountNumber
        ) {}
    }
    public record RegisterResponseDTO(
            @JsonProperty("message") String message,
            @JsonProperty("user_id") String userId,
            @JsonProperty("token") String token
    ) {}
    public record LoginRequestDTO(
            @JsonProperty("phone") String phone,
            @JsonProperty("password") String password
    ) {}
    public record UserSchemaDTO(
            @JsonProperty("id") String id,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("role") String role,
            @JsonProperty("address") String address,
            @JsonProperty("profileImageBase64") String profileImageBase64,
            @JsonProperty("bank_info") RegisterRequestDTO.BankInfoDTO bankInfo
    ) {}
    public record LoginResponseDTO(
            @JsonProperty("message") String message,
            @JsonProperty("token") String token,
            @JsonProperty("user") UserSchemaDTO user
    ) {}
    public record ErrorResponseDTO(
            @JsonProperty("error") String error
    ) {}
}