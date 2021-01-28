package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.service.LoginService;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.AccountCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.Serializable;

@RestController
@RequestMapping("/login")
@Validated
@RequiredArgsConstructor
public class LoginRestController implements Serializable {

    private final static ResponseEntity<Object> UNAUTHORIZED = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    private final LoginService service;

    @PostMapping
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ResponseEntity> login(@Valid @RequestBody final AccountCredentials accountCredentials) {
        return service.authenticate(accountCredentials).map(jwt -> StringUtils.hasText(jwt) ? createHttpHeaders(jwt) : UNAUTHORIZED );
    }

    private ResponseEntity createHttpHeaders(final String jwt) {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, jwt);
        return ResponseEntity.ok().headers(httpHeaders).build();
    }

}
