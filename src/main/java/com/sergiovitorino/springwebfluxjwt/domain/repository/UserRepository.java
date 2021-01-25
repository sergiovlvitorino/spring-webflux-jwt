package com.sergiovitorino.springwebfluxjwt.domain.repository;

import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByEmail(String email);

    Flux<User> findByRoleId(String id);

    Mono<Boolean> existsByEmail(String email);
}
