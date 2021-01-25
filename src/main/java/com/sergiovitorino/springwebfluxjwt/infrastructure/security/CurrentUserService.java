package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final SecurityContextRepository securityContextRepository;

    public Mono<String> getCurrentUser(final ServerWebExchange exchange){
        return securityContextRepository
                .load(exchange)
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(String.class);
    }

}
