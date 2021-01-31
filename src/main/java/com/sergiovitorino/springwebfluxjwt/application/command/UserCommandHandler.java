package com.sergiovitorino.springwebfluxjwt.application.command;

import com.sergiovitorino.springwebfluxjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springwebfluxjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springwebfluxjwt.application.service.UserService;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserCommandHandler {

    private final UserService service;

    public Mono<User> execute(SaveCommand command) {
        final var user = new User();
        user.setName(command.getName());
        user.setEmail(command.getEmail());
        user.setPassword(command.getPassword());
        user.setRole(new Role());
        user.getRole().setId(command.getRoleId());
        return service.save(user);
    }

    public Flux<User> execute(FindAllCommand command) {
        return service.findAll();
    }

    public Mono<User> execute(FindByIdCommand command) {
        return service.find(command.getId());
    }

    public Mono<User> execute(UpdateCommand command) {
        final var user = new User();
        user.setId(command.getId());
        user.setName(command.getName());
        user.setEmail(command.getEmail());
        user.setPassword(command.getPassword());
        user.setRole(new Role());
        user.getRole().setId(command.getRoleId());
        return service.update(user);
    }
}
