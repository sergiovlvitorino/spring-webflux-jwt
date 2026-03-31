package com.sergiovitorino.springwebfluxjwt.domain.document;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testAllArgsConstructor() {
        var role = new Role("r1", "ADMIN", List.of(new Authority("SAVE_USER")));
        var user = new User("id1", "John", "john@test.com", "pass", true, role);

        assertEquals("id1", user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertTrue(user.isEnabled());
        assertEquals(role, user.getRole());
    }

    @Test
    void testGetUsername() {
        var user = new User();
        user.setEmail("test@email.com");
        assertEquals("test@email.com", user.getUsername());
    }

    @Test
    void testUserDetailsDefaults() {
        var user = new User();
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testGetAuthorities() {
        var role = new Role();
        role.setAuthorities(List.of(new Authority("SAVE_USER"), new Authority("RETRIEVE_USER")));
        var user = new User();
        user.setRole(role);

        var authorities = user.getAuthorities();
        assertEquals(2, authorities.size());
    }

    @Test
    void testEncodePassword() {
        var user = new User();
        user.setPassword("plaintext");
        user.encodePassword(new BCryptPasswordEncoder());

        assertNotEquals("plaintext", user.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("plaintext", user.getPassword()));
    }

    @Test
    void testEqualsAndHashCodeById() {
        var role = new Role("r1", "ADMIN", List.of());
        var user1 = new User("id1", "John", "john@test.com", "pass", true, role);
        var user2 = new User("id1", "Jane", "jane@test.com", "other", false, role);
        var user3 = new User("id2", "Jane", "jane@test.com", "pass", true, role);

        assertEquals(user1, user2, "Users with same id should be equal");
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
        assertNotEquals(user1, null);
        assertNotEquals(user1, "string");
        assertEquals(user1, user1);
    }

    @Test
    void testToString() {
        var user = new User();
        user.setId("id1");
        user.setName("John");
        user.setEmail("john@test.com");
        user.setEnabled(true);

        var str = user.toString();
        assertTrue(str.contains("id1"));
        assertTrue(str.contains("John"));
    }
}
