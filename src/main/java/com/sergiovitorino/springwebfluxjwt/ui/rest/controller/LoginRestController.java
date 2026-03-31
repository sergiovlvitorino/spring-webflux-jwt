package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.service.LoginService;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.AccountCredentials;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/login")
@Validated
public class LoginRestController {

    private static final ResponseEntity<Object> UNAUTHORIZED = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    private final LoginService service;

    public LoginRestController(LoginService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> login(@Valid @RequestBody final AccountCredentials accountCredentials) {
        return service.authenticate(accountCredentials)
                .map(jwt -> createHttpHeaders("Bearer " + jwt))
                .defaultIfEmpty(UNAUTHORIZED);
    }

    private ResponseEntity<Object> createHttpHeaders(final String authorizationHeader) {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authorizationHeader);
        return ResponseEntity.ok().headers(httpHeaders).build();
    }
}
