package com.sergiovitorino.springwebfluxjwt.ui.rest.controller;

import com.sergiovitorino.springwebfluxjwt.application.command.FindAllCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.FindByIdCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.UserCommandHandler;
import com.sergiovitorino.springwebfluxjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserRestController {

    private final UserCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('SAVE_USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> post(@Valid @RequestBody final SaveCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('RETRIEVE_USER')")
    @GetMapping
    public Flux<User> get(){
        return commandHandler.execute(new FindAllCommand());
    }

    @PreAuthorize("hasAuthority('RETRIEVE_USER')")
    @GetMapping("/{id}")
    public Mono<User> get(@PathVariable("id") final String id){
        return commandHandler.execute(new FindByIdCommand(id));
    }

}
