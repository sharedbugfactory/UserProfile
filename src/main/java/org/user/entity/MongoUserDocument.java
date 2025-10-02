package org.user.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@Getter
@Setter
public class MongoUserDocument {
    @Id
    private String id;
    private String name;

    @Indexed(unique = true)
    private String aadharNumber;

    public MongoUserDocument() {}
    public MongoUserDocument(String id, String name, String aadharNumber) {
        this.id = id; this.name = name; this.aadharNumber = aadharNumber;
    }
}
