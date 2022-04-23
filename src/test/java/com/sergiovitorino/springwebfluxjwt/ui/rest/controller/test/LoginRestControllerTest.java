package com.sergiovitorino.springwebfluxjwt.ui.rest.controller.test;

import com.sergiovitorino.springwebfluxjwt.application.service.UserService;
import com.sergiovitorino.springwebfluxjwt.domain.document.Authority;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.4.5")
public class LoginRestControllerTest {

    @Autowired private WebTestClient webTestClient;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeAll
    public void setUp() {
        final var role = new Role();
        role.setName("ADMIN");
        role.setAuthorities(new ArrayList<>());
        role.getAuthorities().add(new Authority("RETRIEVE_USER"));
        role.getAuthorities().add(new Authority("SAVE_USER"));
        role.getAuthorities().add(new Authority("RETRIEVE_ROLE"));

        final var user = new User();
        user.setName("login login");
        user.setEmail("login@gmail.com");
        user.setPassword("123456");
        user.setRole(roleRepository.save(role).block());

        userService.save(user).block();
    }

    @AfterAll
    public void teardown() {
        userRepository.deleteAll().block();
        roleRepository.deleteAll().block();
    }

    @Test
    void testIfLoginIsOk() {
        final var credentials = "{\"username\":\"login@gmail.com\",\"password\":\"123456\"}";
        final var body = BodyInserters.fromValue(credentials);
        webTestClient
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .exists(HttpHeaders.AUTHORIZATION);
    }

    @Test
    void testIfLoginReturnsUnauthorizedWhenUserNotExists() {
        final var credentials = "{\"username\":\"unauthorized@gmail.com\",\"password\":\"123456\"}";
        final var body = BodyInserters.fromValue(credentials);
        webTestClient
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void testIfLoginReturnsBadRequestWhenUsernameIsEmpty() {
        final var credentials = "{\"username\":\"\",\"password\":\"123456\"}";
        final var body = BodyInserters.fromValue(credentials);
        webTestClient
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void testIfLoginReturnsBadRequestWhenUsernameIsNotAnEmail() {
        final var credentials = "{\"username\":\"johndoegmail.com\",\"password\":\"123456\"}";
        final var body = BodyInserters.fromValue(credentials);
        webTestClient
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    void testIfLoginReturnsBadRequestWhenPasswordIsEmpty() {
        final var credentials = "{\"username\":\"johndoe@gmail.com\",\"password\":\"\"}";
        final var body = BodyInserters.fromValue(credentials);
        webTestClient
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

}
