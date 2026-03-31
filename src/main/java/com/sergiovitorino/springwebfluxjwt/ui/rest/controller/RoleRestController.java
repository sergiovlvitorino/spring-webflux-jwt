package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.command.FindAllCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.FindByIdCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.RoleCommandHandler;
import com.sergiovitorino.springwebfluxjwt.application.dto.PageResponse;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/role")
public class RoleRestController {

    private final RoleCommandHandler commandHandler;

    public RoleRestController(RoleCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PreAuthorize("hasAuthority('RETRIEVE_ROLE')")
    @GetMapping
    public Mono<PageResponse<Role>> get(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return commandHandler.execute(new FindAllCommand(page, size));
    }

    @PreAuthorize("hasAuthority('RETRIEVE_ROLE')")
    @GetMapping("/{id}")
    public Mono<Role> get(@PathVariable("id") final String id) {
        return commandHandler.execute(new FindByIdCommand(id));
    }
}
