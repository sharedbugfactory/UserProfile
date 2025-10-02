package org.user.router;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.user.repository.UserRepository;

@Component
public class UserRepoRouter {
    private final UserRepository psql;
    private final UserRepository mongo;

    public UserRepoRouter(@Qualifier("postgres") UserRepository psql,
                          @Qualifier("mongo") UserRepository mongo) {
        this.psql = psql;
        this.mongo = mongo;
    }

    public UserRepository pick(String db) {
        return db != null && !db.isEmpty()
                && "mongo".equalsIgnoreCase(db) ? mongo : psql; // default to Postgres
    }
}
