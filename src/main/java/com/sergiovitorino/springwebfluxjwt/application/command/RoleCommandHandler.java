package com.sergiovitorino.springwebfluxjwt.application.command;

import com.sergiovitorino.springwebfluxjwt.application.service.RoleService;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RoleCommandHandler {

    private final RoleService service;

    public RoleCommandHandler(RoleService service) {
        this.service = service;
    }

    public Flux<Role> execute(FindAllCommand command) {
        return service.findAll();
    }

    public Mono<Role> execute(FindByIdCommand command) {
        return service.find(command.id());
    }
}
