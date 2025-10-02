package org.user.persistence;

import org.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserEntityRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByAadharNumber(String aadhar);
    boolean existsByAadharNumber(String aadhar);
}
