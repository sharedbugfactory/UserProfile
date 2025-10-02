package org.user.persistence;

import org.springframework.beans.factory.annotation.Qualifier;
import org.user.entity.UserEntity;
import org.user.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.user.repository.UserRepository;

import java.util.UUID;

@Repository
@Transactional
@Qualifier("postgres")
public class PostgresUserRepository implements UserRepository {

    private final JpaUserEntityRepository jpa;

    public PostgresUserRepository(JpaUserEntityRepository jpa) {
        this.jpa = jpa;
    }

    private static User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .name(e.getName())
                .aadharNumber(e.getAadharNumber())
                .build();
    }

    @Override
    public String addUser(String name, String aadhar) {
        // enforce uniqueness at app level (DB constraint also exists)
        if (jpa.existsByAadharNumber(aadhar)) {
            throw new IllegalArgumentException("Aadhar already exists");
        }
        String id = UUID.randomUUID().toString();
        UserEntity entity = new UserEntity(id, name, aadhar);
        jpa.save(entity);
        return id;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(String id) {
        UserEntity e = jpa.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toDomain(e);
    }

    @Override
    public boolean updateUserName(String id, String name) {
        UserEntity e = jpa.findById(id).orElse(null);
        if (e == null)
            return false;
        e.setName(name);
        jpa.save(e);
        return true;
    }

    @Override
    public boolean updateUserAadhar(String id, String aadhar) {
        var e = jpa.findById(id).orElse(null);
        if (e == null)
            return false;
        // avoid duplicate aadhar
        if (jpa.existsByAadharNumber(aadhar) && !aadhar.equals(e.getAadharNumber())) {
            throw new IllegalArgumentException("Aadhar already exists");
        }
        e.setAadharNumber(aadhar);
        jpa.save(e);
        return true;
    }
}
