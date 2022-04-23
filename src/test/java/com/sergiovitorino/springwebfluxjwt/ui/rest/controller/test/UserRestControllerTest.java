package com.sergiovitorino.springwebfluxjwt.ui.rest.controller.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springwebfluxjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springwebfluxjwt.application.service.UserService;
import com.sergiovitorino.springwebfluxjwt.domain.document.Authority;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.4.5")
class UserRestControllerTest {

    @Autowired private UserService userService;
    @Autowired private WebTestClient webTestClient;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper mapper;
    @LocalServerPort private int port;
    private static User user;
    private static Role role;
    private static HttpHeaders httpHeaders;

    @BeforeAll
    public void setUp() {
        role = new Role();
        role.setName("ADMIN");
        role.setAuthorities(new ArrayList<>());
        role.getAuthorities().add(new Authority("RETRIEVE_USER"));
        role.getAuthorities().add(new Authority("SAVE_USER"));
        role.getAuthorities().add(new Authority("RETRIEVE_ROLE"));
        role = roleRepository.save(role).block();

        final var userInner = new User();
        userInner.setName("sergio vitorino");
        userInner.setEmail("sergiovlvitorino@gmail.com");
        userInner.setPassword("123456");
        userInner.setRole(role);

        user = userService.save(userInner).block();

        final var credentials = "{\"username\":\"sergiovlvitorino@gmail.com\",\"password\":\"123456\"}";
        final var body = BodyInserters.fromValue(credentials);
        httpHeaders = WebClient
                .builder()
                .baseUrl("http://localhost:" + port)
                .build()
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity()
                .block()
                .getHeaders();
    }

    @AfterAll
    public void teardown() {
        roleRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    void testIfPostIsOK() throws JsonProcessingException {
        final var command = new SaveCommand();
        command.setName("Lorem Ipsum");
        command.setEmail("lorem@ipsum.com");
        command.setPassword("123456");
        command.setRoleId(role.getId());
        final var body = BodyInserters.fromValue(mapper.writeValueAsString(command));

        final var userResult = webTestClient
                .post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertEquals(command.getEmail(), userResult.getEmail());
        userRepository.delete(userResult).block();

    }

    @Test
    void testIfGetIsOK() {
        webTestClient
                .get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(User.class)
                .hasSize(1);

    }

    @Test
    void testIfGetByIdIsOK() {
        webTestClient
                .get()
                .uri("/user/{id}", user.getId())
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(user.getId());
    }

    @Test
    void testIfGetCurrentUser() {
        webTestClient
                .get()
                .uri("/user/currentUser")
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(user.getUsername());
    }


}
