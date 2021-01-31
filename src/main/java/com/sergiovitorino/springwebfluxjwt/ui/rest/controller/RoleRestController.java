package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.command.FindAllCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.FindByIdCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.RoleCommandHandler;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleRestController implements Serializable {

    private final RoleCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('RETRIEVE_ROLE')")
    @GetMapping
    public Flux<Role> get(){
        return commandHandler.execute(new FindAllCommand());
    }

    @PreAuthorize("hasAuthority('RETRIEVE_ROLE')")
    @GetMapping("/{id}")
    public Mono<Role> get(@PathVariable("id") final String id){ return commandHandler.execute(new FindByIdCommand(id)); }

}
