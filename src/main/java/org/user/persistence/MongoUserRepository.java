package org.user.persistence;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.user.entity.MongoUserDocument;
import org.user.model.User;
import org.user.repository.UserRepository;

import java.util.UUID;

@Repository
@Transactional
@Qualifier("mongo")
public class MongoUserRepository implements UserRepository {
    private final MongoUserEntityRepository mongo;

    public MongoUserRepository(MongoUserEntityRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public String addUser(String name, String aadhar) {
        var id = UUID.randomUUID().toString();
        mongo.save(new MongoUserDocument(id, name, aadhar));
        return id;
    }

    @Override
    public User getUser(String id) {
        var d = mongo.findById(id).orElseThrow();
        return User.builder().id(d.getId()).name(d.getName()).aadharNumber(d.getAadharNumber()).build();
    }

    @Override
    public boolean updateUserName(String id, String name) {
        return mongo.findById(id).map(d -> { d.setName(name); mongo.save(d); return true; }).orElse(false);
    }

    @Override
    public boolean updateUserAadhar(String id, String aadhar) {
        return mongo.findById(id).map(d -> { d.setAadharNumber(aadhar); mongo.save(d); return true; }).orElse(false);
    }
}
