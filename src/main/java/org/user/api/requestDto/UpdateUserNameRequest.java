package org.user.api.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserNameRequest {
    @NotBlank
    private String name;

    public UpdateUserNameRequest() {}
    public UpdateUserNameRequest(String name) { this.name = name; }
}
