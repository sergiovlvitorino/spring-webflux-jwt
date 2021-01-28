package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class AccountCredentials implements Serializable {

    @NotEmpty
    @Email
    private String username;
    @NotEmpty
    @Size(min = 6, max = 16)
    private String password;

}
