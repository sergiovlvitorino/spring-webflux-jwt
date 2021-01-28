package com.sergiovitorino.springwebfluxjwt.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

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
