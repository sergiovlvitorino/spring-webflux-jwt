package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoleService {

    private final RoleRepository repository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Mono<Role> save(final Role role) {
        return repository.save(role);
    }

    public Mono<Role> update(final Role role) {
        return repository.existsById(role.getId())
                .flatMap(exists -> {
                    if (!exists)
                        return Mono.error(new IllegalArgumentException("Role not found"));
                    return repository.save(role)
                            .flatMap(roleUpdated ->
                                    userRepository.findByRoleId(roleUpdated.getId())
                                            .flatMap(user -> {
                                                user.setRole(roleUpdated);
                                                return userRepository.save(user);
                                            })
                                            .then(Mono.just(roleUpdated))
                            );
                });
    }

    public Flux<Role> findAll() {
        return repository.findAll();
    }

    public Mono<Role> find(final String id) {
        return repository.findById(id);
    }

}
