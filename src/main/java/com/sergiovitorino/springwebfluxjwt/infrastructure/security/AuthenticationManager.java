package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager, Serializable {

    private final JWTService jwtService;

    public AuthenticationManager(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(final Authentication authentication) {
        final var authToken = authentication.getCredentials().toString();
        if (authToken != null) {
            final var claims = jwtService.getClaimsFromToken(authToken);
            if (jwtService.isValid(claims)) {
                final var username = claims.get("sub").toString();
                final var authorities = claims.get("authorities").toString();
                final var authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
                final var authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
                return Mono.just(authenticationToken);
            }
        }
        return Mono.empty();
    }

}
