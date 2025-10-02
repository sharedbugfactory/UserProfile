package org.user.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.user.entity.MongoUserDocument;

import java.util.Optional;

public interface MongoUserEntityRepository extends MongoRepository<MongoUserDocument, String> {
    Optional<MongoUserDocument> findByAadharNumber(String aadharNumber);
    boolean existsByAadharNumber(String aadharNumber);
}
