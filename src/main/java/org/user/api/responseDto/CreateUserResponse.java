package org.user.api.responseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserResponse {
    private String id;

    public CreateUserResponse() {}
    public CreateUserResponse(String id) { this.id = id; }
}
