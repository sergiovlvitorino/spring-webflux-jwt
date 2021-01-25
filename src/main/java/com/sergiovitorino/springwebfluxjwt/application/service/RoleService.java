package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.SecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repository;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;

    public Mono<Role> save(final Role role) {
        return repository.save(role);
    }

    public Mono<Role> update(final Role role) {
        return repository.existsById(role.getId())
                .flatMap(result -> {
                    if (!result)
                        throw new IllegalArgumentException("Role not found");
                    return repository.save(role)
                            .doOnNext(roleUpdated -> {
                                userRepository
                                        .findByRoleId(roleUpdated.getId())
                                        .doOnNext(user -> {
                                            user.setRole(roleUpdated);
                                            userRepository.save(user);
                                        }).then(Mono.just(roleUpdated));
                            });
                });
    }

    public Flux<Role> findAll() {
        return repository.findAll();
    }

    public Mono<Role> find(final String id) {
        return repository.findById(id);
    }

}
