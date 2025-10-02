package org.user.api.responseDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserResponse {
    private boolean updated;

    public UpdateUserResponse() {}
    public UpdateUserResponse(boolean updated) { this.updated = updated; }
}
