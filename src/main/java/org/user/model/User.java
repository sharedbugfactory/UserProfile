package org.user.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class User {

    private String id;
    private String name;
    private String aadharNumber;
}
