package com.sergiovitorino.springwebfluxjwt.application.command.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateCommand(
        @NotEmpty String id,
        @NotEmpty String name,
        @Email String email,
        @NotEmpty @Size(min = 6, max = 16) String password,
        @NotEmpty String roleId
) {
}
