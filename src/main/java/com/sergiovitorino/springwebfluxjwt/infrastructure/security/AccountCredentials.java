package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AccountCredentials(
        @NotEmpty @Email String username,
        @NotEmpty @Size(min = 6, max = 16) String password
) {
}
