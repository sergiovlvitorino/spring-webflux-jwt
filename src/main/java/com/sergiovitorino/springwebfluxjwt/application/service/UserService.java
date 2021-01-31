package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Cacheable(cacheNames = "user", key = "{#root.method.name,#email}")
    @Override
    public Mono<UserDetails> findByUsername(final String email) {
        return repository.findByEmail(email).cast(UserDetails.class);
    }

    @Cacheable(cacheNames = "user", key = "#root.method.name")
    public Flux<User> findAll() {
        return repository.findAll();
    }

    @CacheEvict(cacheNames = "user")
    public Mono<User> save(final User user) {
        user.setEnabled(true);
        user.encodePassword(passwordEncoder);
        return repository.existsByEmail(user.getEmail()).flatMap(result -> {
            if (result)
                throw new IllegalArgumentException("Email already");
            return roleService
                    .find(user.getRole().getId())
                    .doOnNext(role -> {
                        if (role == null)
                            throw new IllegalArgumentException("Role not found");
                        user.setRole(role);
                    }).then(repository.save(user));
        });
    }

    @CacheEvict(cacheNames = "user", allEntries = true)
    public Mono<User> update(final User user) {
        return repository
                .existsById(user.getId())
                .flatMap(result -> {
                    if (!result)
                        throw new IllegalArgumentException("User not found");
                    return roleService.find(user.getRole().getId())
                            .doOnNext(role -> {
                                if (role == null)
                                    throw new IllegalArgumentException("Role not found");
                                user.setRole(role);
                            })
                            .flatMap(role -> repository.save(user));
                });
    }

    @Cacheable(cacheNames = "user", key = "{#root.method.name,#id}")
    public Mono<User> find(final String id) {
        return repository.findById(id);
    }

}