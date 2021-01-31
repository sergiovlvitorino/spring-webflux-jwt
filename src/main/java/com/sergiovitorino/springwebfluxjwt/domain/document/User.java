package com.sergiovitorino.springwebfluxjwt.domain.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.Collection;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class User implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String name;
    private String email;
    @JsonIgnore
    private String password;
    @Getter(AccessLevel.NONE)
    private boolean enabled;
    private Role role;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils
                .createAuthorityList(
                        getRole()
                                .getAuthorities()
                                .stream()
                                .map(Authority::getName)
                                .toArray(String[]::new)
                );
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void encodePassword(final PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

}