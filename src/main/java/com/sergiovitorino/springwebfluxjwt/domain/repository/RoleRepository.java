package com.sergiovitorino.springwebfluxjwt.domain.repository;

import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends ReactiveMongoRepository<Role, String> {
}
