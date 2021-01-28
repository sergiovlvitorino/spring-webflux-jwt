package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository, Serializable {

    private final AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        final var authHeader = exchange
                .getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final var authToken = authHeader.substring(7);
            final var auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
            return authenticationManager
                    .authenticate(auth)
                    .map(SecurityContextImpl::new);
        }

        return Mono.empty();
    }

}
