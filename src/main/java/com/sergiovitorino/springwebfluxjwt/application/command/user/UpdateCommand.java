package com.sergiovitorino.springwebfluxjwt.application.command.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class UpdateCommand {

    @NotEmpty
    private String id;
    @NotEmpty
    private String name;
    @Email
    private String email;
    @NotEmpty
    @Size(min = 6, max = 16)
    private String password;
    @NotEmpty
    private String roleId;

}
