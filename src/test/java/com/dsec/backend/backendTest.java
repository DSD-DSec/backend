package com.dsec.backend;

import com.dsec.backend.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import javax.naming.AuthenticationException;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// The test profile allow tests to run having db always clean
@ActiveProfiles("test")
public class backendTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/users returns unauthorized if the user is not logged in")
    public void getUsersUnauthorizedTest(){

        // GET request to retrieve all the users
        ResponseEntity<String> response =
                this.restTemplate.getForEntity("http://localhost:" + port + "/api/users", String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    @DisplayName("POST /api/auth/register -> /api/auth/login returns ok response")
    public void registrationLoginOkTest() throws Exception {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registration parameters of the user
        JSONObject registerParameters = new JSONObject();
        registerParameters.put("firstName", "Bob");
        registerParameters.put("lastName", "Miller");
        registerParameters.put("email", "bob.miller@gmail.com");
        registerParameters.put("password", "Password90!");
        registerParameters.put("secondPassword", "Password90!");

        HttpEntity<String> registerRequest =
                new HttpEntity<String>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse =
                this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", registerRequest, UserEntity.class);

        // Checking that the register response returns the right parameter of the right user
        assertThat(registerResponse.getBody().getFirstName()).isEqualTo("Bob");
        assertThat(registerResponse.getBody().getLastName()).isEqualTo("Miller");
        assertThat(registerResponse.getBody().getEmail()).isEqualTo("bob.miller@gmail.com");

        // Login parameter of the previous registered user
        JSONObject loginParameters = new JSONObject();
        loginParameters.put("email", "bob.miller@gmail.com");
        loginParameters.put("password", "Password90!");

        HttpEntity<String> loginRequest =
                new HttpEntity<String>(loginParameters.toString(), headers);

        // POST request to log in the user
        ResponseEntity<UserEntity> loginResponse =
                this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/login", loginRequest, UserEntity.class);

        // Checking that the log in response returns the right parameter of the right user
        assertThat(loginResponse.getBody().getFirstName()).isEqualTo("Bob");
        assertThat(loginResponse.getBody().getLastName()).isEqualTo("Miller");
        assertThat(loginResponse.getBody().getEmail()).isEqualTo("bob.miller@gmail.com");

        // Expected ok since the user has registered an account and login parameters are correct
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    @DisplayName("GET /api/auth/login returns an exception if the user does not exist")
    public void unauthorizedLoginTest() throws Exception {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Login parameter of a non-existing user
        JSONObject loginParameters = new JSONObject();
        loginParameters.put("email", "bob.miller@gmail.com");
        loginParameters.put("password", "pass");

        HttpEntity<String> loginRequest =
                new HttpEntity<String>(loginParameters.toString(), headers);

        // Resource Access Exception is expected since the user does not exist
        assertThrows(ResourceAccessException.class,
                ()->{
                    // POST request to log in the user
                    ResponseEntity<UserEntity> loginResponse = this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/login",
                            loginRequest, UserEntity.class);
                });

    }

    @Test
    @DisplayName("POST /api/auth/register returns bad request if the password/email constraints are not respected")
    public void wrongRegisterTest() throws JSONException {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registration parameters of the user
        JSONObject registerParameters = new JSONObject();
        registerParameters.put("firstName", "Bob");
        registerParameters.put("lastName", "Miller");
        registerParameters.put("email", "bob.miller@gmail.com");
        registerParameters.put("password", "Password90!");
        // Password parameter does not respect contraints
        registerParameters.put("secondPassword", "pass!");

        HttpEntity<String> registerRequest =
                new HttpEntity<String>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse =
                this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", registerRequest, UserEntity.class);

        // Expected Bad Request since the user has not respected password constraints
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }




}
