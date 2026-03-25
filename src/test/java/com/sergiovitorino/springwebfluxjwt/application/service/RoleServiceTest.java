package com.sergiovitorino.springwebfluxjwt.application.service;

import com.sergiovitorino.springwebfluxjwt.domain.document.Authority;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoleServiceTest {

    @Autowired private RoleService roleService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    @AfterAll
    void teardown() {
        userRepository.deleteAll().block();
        roleRepository.deleteAll().block();
    }

    @Test
    void testSaveRole() {
        var role = new Role();
        role.setName("TEST_ROLE");
        role.setAuthorities(List.of(new Authority("TEST_AUTH")));

        var saved = roleService.save(role).block();
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("TEST_ROLE", saved.getName());

        roleRepository.delete(saved).block();
    }

    @Test
    void testFindAll() {
        var role = new Role();
        role.setName("FIND_ALL_ROLE");
        role.setAuthorities(List.of());
        roleRepository.save(role).block();

        var roles = roleService.findAll().collectList().block();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());

        roleRepository.delete(role).block();
    }

    @Test
    void testFindById() {
        var role = new Role();
        role.setName("FIND_BY_ID_ROLE");
        role.setAuthorities(List.of());
        var saved = roleRepository.save(role).block();

        var found = roleService.find(saved.getId()).block();
        assertNotNull(found);
        assertEquals("FIND_BY_ID_ROLE", found.getName());

        roleRepository.delete(saved).block();
    }

    @Test
    void testUpdateRole() {
        var role = new Role();
        role.setName("ORIGINAL");
        role.setAuthorities(new ArrayList<>(List.of(new Authority("SAVE_USER"))));
        var saved = roleRepository.save(role).block();

        saved.setName("UPDATED");
        var updated = roleService.update(saved).block();
        assertNotNull(updated);
        assertEquals("UPDATED", updated.getName());

        roleRepository.delete(updated).block();
    }

    @Test
    void testUpdateRoleNotFoundThrows() {
        var role = new Role();
        role.setId("nonexistent-id");
        role.setName("GHOST");
        role.setAuthorities(List.of());

        assertThrows(IllegalArgumentException.class, () -> roleService.update(role).block());
    }

    @Test
    void testUpdateRolePropagatesRoleToUsers() {
        var role = new Role();
        role.setName("PROPAGATE");
        role.setAuthorities(new ArrayList<>(List.of(new Authority("SAVE_USER"), new Authority("RETRIEVE_USER"))));
        role = roleRepository.save(role).block();

        var user = new User();
        user.setName("Propagation User");
        user.setEmail("propagate@test.com");
        user.setPassword("123456");
        user.setRole(role);
        user = userService.save(user).block();

        role.setName("PROPAGATED");
        roleService.update(role).block();

        var updatedUser = userRepository.findById(user.getId()).block();
        assertEquals("PROPAGATED", updatedUser.getRole().getName());

        userRepository.delete(updatedUser).block();
        roleRepository.delete(role).block();
    }
}
