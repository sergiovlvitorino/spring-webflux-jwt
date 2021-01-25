package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Override
    public Mono<UserDetails> findByUsername(final String email) { return repository.findByEmail(email).cast(UserDetails.class); }

    public Flux<User> findAll() { return repository.findAll(); }

    public Mono<User> save(final User user) {
        user.setEnabled(true);
        user.encodePassword(passwordEncoder);
        return roleService.find(user.getRole().getId())
                .doOnNext(role -> user.setRole(role))
                .flatMap(role -> repository.save(user));
    }

    public Mono<User> update(final User user) {
        return repository.save(user);
    }


    public Mono<User> find(final String id) {
        return repository.findById(id);
    }

    public Flux<User> findByRoleId(String id) {
        return repository.findByRoleId(id);
    }
}
