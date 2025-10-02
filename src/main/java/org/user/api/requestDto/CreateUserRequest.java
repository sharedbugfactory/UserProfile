package org.user.api.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateUserRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String aadharNumber;

    public CreateUserRequest() {}

    public CreateUserRequest(String name, String aadharNumber) {
        this.name = name;
        this.aadharNumber = aadharNumber;
    }
}