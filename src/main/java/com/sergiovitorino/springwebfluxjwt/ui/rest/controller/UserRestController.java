package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.command.FindAllCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.FindByIdCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.UserCommandHandler;
import com.sergiovitorino.springwebfluxjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.infrastructure.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.Serializable;

@RestController
@RequestMapping("/user")
@Validated
@RequiredArgsConstructor
public class UserRestController implements Serializable {

    private final UserCommandHandler commandHandler;
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasAuthority('SAVE_USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> post(@Valid @RequestBody final SaveCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('SAVE_USER')")
    @PutMapping
    public Mono<User> put(@Valid @RequestBody final UpdateCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('RETRIEVE_USER')")
    @GetMapping
    public Flux<User> findAll(){
        return commandHandler.execute(new FindAllCommand());
    }

    @PreAuthorize("hasAuthority('RETRIEVE_USER')")
    @GetMapping("/{id}")
    public Mono<User> findById(@PathVariable("id") final String id){
        return commandHandler.execute(new FindByIdCommand(id));
    }

    @PreAuthorize("hasAuthority('RETRIEVE_USER')")
    @GetMapping("/currentUser")
    public Mono<String> findCurrentUser(final ServerWebExchange exchange){
        return currentUserService.getCurrentUser(exchange);
    }
}
