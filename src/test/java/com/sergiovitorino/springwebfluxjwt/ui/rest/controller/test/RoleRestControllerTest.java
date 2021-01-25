package com.sergiovitorino.springwebfluxjwt.ui.rest.controller.test;

import com.sergiovitorino.springwebfluxjwt.application.service.UserService;
import com.sergiovitorino.springwebfluxjwt.domain.document.Authority;
import com.sergiovitorino.springwebfluxjwt.domain.document.Role;
import com.sergiovitorino.springwebfluxjwt.domain.document.User;
import com.sergiovitorino.springwebfluxjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springwebfluxjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleRestControllerTest {

    @Autowired private UserService userService;
    @Autowired private WebTestClient webTestClient;
    @LocalServerPort private int port;
    private static User user;
    private static Role role;
    private static HttpHeaders httpHeaders;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    private static int countTests = 0;
    private static final int TOTAL_TESTS = 2;

    @BeforeEach
    public void setUp(){
        countTests++;
        if (user == null){
            role = new Role();
            role.setName("ADMIN");
            role.setAuthorities(new ArrayList<>());
            role.getAuthorities().add(new Authority("RETRIEVE_USER"));
            role.getAuthorities().add(new Authority("SAVE_USER"));
            role.getAuthorities().add(new Authority("RETRIEVE_ROLE"));
            role = roleRepository.save(role).block();

            user = new User();
            user.setName("sergio vitorino");
            user.setEmail("sergiovlvitorino@gmail.com");
            user.setPassword("123456");
            user.setRole(role);

            user = userService.save(user).block();

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
    }

    @AfterEach
    public void teardown(){
        if (TOTAL_TESTS == 2){
            roleRepository.deleteAll().block();
            userRepository.deleteAll().block();
            user = null;
        }
    }

    @Test
    void testIfGetIsOK() {
        webTestClient
                .get()
                .uri("/role")
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Role.class)
                .hasSize(1);
    }

    @Test
    void testIfGetByIdIsOK() {
        var roleResult = webTestClient
                .get()
                .uri("/role/{id}", role.getId())
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Role.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertEquals(role.getId(), roleResult.getId());
    }
}