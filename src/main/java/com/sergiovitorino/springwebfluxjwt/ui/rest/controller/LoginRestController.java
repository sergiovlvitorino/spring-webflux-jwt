package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.service.LoginService;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.AccountCredentials;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.io.Serializable;

@RestController
@RequestMapping("/login")
@Validated
public class LoginRestController implements Serializable {

    private final static ResponseEntity<Object> UNAUTHORIZED = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    private final LoginService service;

    public LoginRestController(LoginService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> login(@Valid @RequestBody final AccountCredentials accountCredentials) {
        return service.authenticate(accountCredentials).map(jwt -> StringUtils.hasText(jwt) ? createHttpHeaders(jwt) : UNAUTHORIZED );
    }

    private ResponseEntity<Object> createHttpHeaders(final String jwt) {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, jwt);
        return ResponseEntity.ok().headers(httpHeaders).build();
    }

}
