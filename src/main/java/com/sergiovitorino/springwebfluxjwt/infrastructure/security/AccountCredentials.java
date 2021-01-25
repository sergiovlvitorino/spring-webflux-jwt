package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class AccountCredentials {

    @NotEmpty
    @Email
    private String username;
    @NotEmpty
    @Size(min = 6, max = 16)
    private String password;

}
