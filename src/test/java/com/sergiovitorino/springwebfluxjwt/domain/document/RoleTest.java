package com.sergiovitorino.springwebfluxjwt.domain.document;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testAllArgsConstructor() {
        var authorities = List.of(new Authority("SAVE_USER"));
        var role = new Role("r1", "ADMIN", authorities);

        assertEquals("r1", role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals(1, role.getAuthorities().size());
    }

    @Test
    void testGetAuthoritiesReturnsEmptyListIfNull() {
        var role = new Role();
        assertNotNull(role.getAuthorities());
        assertTrue(role.getAuthorities().isEmpty());
    }

    @Test
    void testSetters() {
        var role = new Role();
        role.setId("r1");
        role.setName("USER");
        role.setAuthorities(List.of(new Authority("READ")));

        assertEquals("r1", role.getId());
        assertEquals("USER", role.getName());
        assertEquals(1, role.getAuthorities().size());
    }

    @Test
    void testEqualsAndHashCodeById() {
        var role1 = new Role("r1", "ADMIN", List.of());
        var role2 = new Role("r1", "USER", List.of(new Authority("X")));
        var role3 = new Role("r2", "USER", List.of());

        assertEquals(role1, role2, "Roles with same id should be equal");
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1, role3);
        assertNotEquals(role1, null);
        assertNotEquals(role1, "string");
        assertEquals(role1, role1);
    }

    @Test
    void testToString() {
        var role = new Role("r1", "ADMIN", List.of());
        var str = role.toString();
        assertTrue(str.contains("r1"));
        assertTrue(str.contains("ADMIN"));
    }
}
