package com.sergiovitorino.springwebfluxjwt.domain.document;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
public class Role {
    @Id
    private String id;
    private String name;
    @Getter(AccessLevel.NONE)
    private List<Authority> authorities;

    public List<Authority> getAuthorities() {
        if (authorities == null)
            authorities = new ArrayList<>();
        return authorities;
    }

}
