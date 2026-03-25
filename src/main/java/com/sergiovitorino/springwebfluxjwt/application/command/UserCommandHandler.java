package com.sergiovitorino.springwebfluxjwt.application.command;

import com.sergiovitorino.springwebfluxjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springwebfluxjwt.application.service.UserService;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class UserCommandHandler {

    private final UserService service;

    public UserCommandHandler(UserService service) {
        this.service = service;
    }

    public Mono<User> execute(SaveCommand command) {
        final var user = new User();
        user.setName(command.name());
        user.setEmail(command.email());
        user.setPassword(command.password());
        user.setRole(new Role());
        user.getRole().setId(command.roleId());
        return service.save(user);
    }

    public Flux<User> execute(FindAllCommand command) {
        return service.findAll();
    }

    public Mono<User> execute(FindByIdCommand command) {
        return service.find(command.id());
    }

    public Mono<User> execute(UpdateCommand command) {
        final var user = new User();
        user.setId(command.id());
        user.setName(command.name());
        user.setEmail(command.email());
        user.setPassword(command.password());
        user.setRole(new Role());
        user.getRole().setId(command.roleId());
        return service.update(user);
    }
}
