package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


@Service
public class UserService implements ReactiveUserDetailsService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public Mono<UserDetails> findByUsername(final String email) {
        return repository.findByEmail(email).cast(UserDetails.class).cache(CACHE_TTL);
    }

    public Flux<User> findAll() {
        return repository.findAll();
    }

    public Mono<User> save(final User user) {
        user.setEnabled(true);
        return Mono.fromCallable(() -> {
                    user.encodePassword(passwordEncoder);
                    return user;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(u -> repository.existsByEmail(u.getEmail())
                        .flatMap(exists -> {
                            if (exists)
                                return Mono.error(new IllegalArgumentException("Email already"));
                            return roleService.find(u.getRole().getId())
                                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                                    .flatMap(role -> {
                                        u.setRole(role);
                                        return repository.save(u);
                                    });
                        })
                );
    }

    public Mono<User> update(final User user) {
        return repository.existsById(user.getId())
                .flatMap(exists -> {
                    if (!exists)
                        return Mono.error(new IllegalArgumentException("User not found"));
                    return roleService.find(user.getRole().getId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                            .flatMap(role -> {
                                user.setRole(role);
                                return repository.save(user);
                            });
                });
    }

    public Mono<User> find(final String id) {
        return repository.findById(id);
    }

}
