package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminDto {
    public record UpdateUserStatusRequestDTO(
            @JsonProperty("status") String status
    ){}

}
