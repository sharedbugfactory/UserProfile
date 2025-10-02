package org.user.api.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserAadharRequest {
    @NotBlank
    private String aadharNumber;

    public UpdateUserAadharRequest() {}
    public UpdateUserAadharRequest(String aadharNumber) { this.aadharNumber = aadharNumber; }
}
