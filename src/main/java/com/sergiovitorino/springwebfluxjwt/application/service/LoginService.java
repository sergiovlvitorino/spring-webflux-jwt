package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.infrastructure.security.AccountCredentials;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.JWTService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class LoginService {

    private final UserService userService;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserService userService, JWTService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<String> authenticate(final AccountCredentials accountCredentials) {
        return userService
                .findByUsername(accountCredentials.username())
                .flatMap(userDetails ->
                        Mono.fromCallable(() -> validate(accountCredentials, userDetails))
                                .subscribeOn(Schedulers.boundedElastic())
                                .filter(valid -> valid)
                                .map(valid -> jwtService.generateToken(userDetails))
                );
    }

    private boolean validate(final AccountCredentials credentials, final UserDetails userDetails) {
        return passwordEncoder.matches(credentials.password(), userDetails.getPassword())
                && userDetails.isEnabled()
                && userDetails.isAccountNonExpired()
                && userDetails.isAccountNonLocked()
                && userDetails.isCredentialsNonExpired();
    }
}
