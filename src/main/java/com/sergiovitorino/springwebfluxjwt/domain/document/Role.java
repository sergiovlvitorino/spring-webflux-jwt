package com.sergiovitorino.springwebfluxjwt.domain.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Document
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String name;
    private List<Authority> authorities;

    public Role() {
    }

    public Role(String id, String name, List<Authority> authorities) {
        this.id = id;
        this.name = name;
        this.authorities = authorities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Authority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(name, role.name) && Objects.equals(authorities, role.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, authorities);
    }

    @Override
    public String toString() {
        return "Role(id=" + id + ", name=" + name + ", authorities=" + authorities + ")";
    }
}
