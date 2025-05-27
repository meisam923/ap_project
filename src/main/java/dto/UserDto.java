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
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("user") UserSchemaDTO user
    ) {}

    public record RegisterResponseDTO(
            @JsonProperty("message") String message,
            @JsonProperty("user_id") String userId,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken
    ) {}
    public record ErrorResponseDTO(
            @JsonProperty("error") String error
    ) {}
    public record UpdateProfileRequestDTO(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("profileImageBase64") String profileImageBase64,
            @JsonProperty("bank_info") RegisterRequestDTO.BankInfoDTO bankInfo
    ) {}
    public record MessageResponseDTO(
            @JsonProperty("message") String message
    ) {}
    public record RefreshTokenRequestDTO(
            @JsonProperty("refresh_token") String refreshToken
    ){}

}
