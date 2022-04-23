package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.infrastructure.security.AccountCredentials;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.Serial;
import java.io.Serializable;

@Service
@RequiredArgsConstructor
public class LoginService implements Serializable {

    private static final long serialVersionUID = 1L;
    private final UserService userService;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Mono<String> authenticate(final AccountCredentials accountCredentials) {
        return userService
                .findByUsername(accountCredentials.getUsername())
                .map(userDetails ->
                        userDetails != null && validate(accountCredentials,userDetails)
                                ? jwtService.generateToken(userDetails)
                                : ""
                );
    }

    private boolean validate(final AccountCredentials credentials, final UserDetails userDetails) {
        return passwordEncoder.matches(credentials.getPassword(), userDetails.getPassword())
                && userDetails.isEnabled()
                && userDetails.isAccountNonExpired()
                && userDetails.isAccountNonLocked()
                && userDetails.isCredentialsNonExpired();
    }

}
