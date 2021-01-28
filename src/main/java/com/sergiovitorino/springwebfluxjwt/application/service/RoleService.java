package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Service
@RequiredArgsConstructor
public class RoleService implements Serializable {

    private static final long serialVersionUID = 1L;
    private final RoleRepository repository;
    private final UserRepository userRepository;

    @CacheEvict(cacheNames = "role", allEntries = true)
    public Mono<Role> save(final Role role) {
        return repository.save(role);
    }

    @CacheEvict(cacheNames = "{user,role}", allEntries = true)
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

    @Cacheable(cacheNames = "role", key = "{#root.method.name}")
    public Flux<Role> findAll() {
        return repository.findAll();
    }

    @Cacheable(cacheNames = "role", key = "{#root.method.name,#id}")
    public Mono<Role> find(final String id) {
        return repository.findById(id);
    }

}
