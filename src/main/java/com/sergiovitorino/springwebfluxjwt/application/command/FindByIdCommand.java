package com.sergiovitorino.springwebfluxjwt.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindByIdCommand {

    @NotEmpty
    private String id;

}
