package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repository;

    private final UserRepository userRepository;

    public Mono<Role> save(final Role role) {
        return repository.save(role);
    }

    public Mono<Role> update(final Role role) {
        return repository.existsById(role.getId()).map(result -> {
            if (!result) {
                throw new IllegalArgumentException("Role not found");
            }
            final var roleUpdated = repository.save(role).block();
            userRepository
                    .findByRoleId(role.getId())
                    .map(user -> {
                        user.setRole(role);
                        return userRepository.save(user);
                    });
            return roleUpdated;
        });
    }

    public Flux<Role> findAll() {
        return repository.findAll();
    }

    public Mono<Role> find(final String id) {
        return repository.findById(id);
    }

}
