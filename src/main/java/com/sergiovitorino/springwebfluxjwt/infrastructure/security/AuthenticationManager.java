package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTService jwtService;

    public AuthenticationManager(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(final Authentication authentication) {
        final var authToken = authentication.getCredentials().toString();
        try {
            final var claims = jwtService.getClaimsFromToken(authToken);
            if (jwtService.isValid(claims)) {
                final var username = claims.get("sub").toString();
                final var authorities = claims.get("authorities").toString();
                final var authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
                final var authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
                return Mono.just(authenticationToken);
            }
            return Mono.error(new BadCredentialsException("Expired or invalid JWT token"));
        } catch (Exception e) {
            return Mono.error(new BadCredentialsException("Invalid JWT token", e));
        }
    }
}
