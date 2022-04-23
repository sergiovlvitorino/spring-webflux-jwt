package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager, Serializable {

    private final JWTService jwtService;

    @Override
    public Mono<Authentication> authenticate(final Authentication authentication) {
        final var authToken = authentication.getCredentials().toString();
        if (authToken != null) {
            final var claims = jwtService.getClaimsFromToken(authToken);
            if (claims.getExpiration().after(Date.from(Instant.now()))) {
                final var username = claims.getSubject();
                final var authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("authorities", String.class));
                final var authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
                return Mono.just(authenticationToken);
            }
        }
        return Mono.empty();
    }

}

