package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        final var authToken = authentication.getCredentials().toString();
        String username;

        try {
            username = jwtService.extractUsername(authToken);
        } catch (Exception e) {
            username = null;
            System.out.println(e);
        }

        if (username != null && jwtService.validateToken(authToken)) {
            final var claims = jwtService.getClaimsFromToken(authToken);
            final var authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("authorities", String.class));
            final var authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
            return Mono.just(authenticationToken);
        } else {
            return Mono.empty();
        }
    }

}

