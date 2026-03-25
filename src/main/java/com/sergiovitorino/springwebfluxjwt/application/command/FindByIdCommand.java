package com.sergiovitorino.springwebfluxjwt.application.command;

import jakarta.validation.constraints.NotEmpty;

public record FindByIdCommand(@NotEmpty String id) {
}
