package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.application.dto.PageResponse;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService implements ReactiveUserDetailsService {

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
        return repository.findByEmail(email).cast(UserDetails.class);
    }

    public Mono<PageResponse<User>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return repository.findAllBy(pageable)
                .collectList()
                .zipWith(repository.count())
                .map(tuple -> PageResponse.of(tuple.getT1(), page, size, tuple.getT2()));
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
                    return Mono.fromCallable(() -> {
                                user.encodePassword(passwordEncoder);
                                return user;
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(u -> roleService.find(u.getRole().getId())
                                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                                    .flatMap(role -> {
                                        u.setRole(role);
                                        return repository.save(u);
                                    }));
                });
    }

    public Mono<User> find(final String id) {
        return repository.findById(id);
    }
}
