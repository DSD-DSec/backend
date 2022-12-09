package com.dsec.backend;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.RepoDomain;
import com.dsec.backend.entity.RepoType;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.repository.RepoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// The test profile allow tests to run having db always clean
@ActiveProfiles("test")
@Slf4j
public class BackendTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepoRepository repoRepository;

    @Test
    @DisplayName("GET /api/users returns unauthorized if the user is not logged in")
    public void getUsersUnauthorizedTest() {

        // GET request to retrieve all the users
        ResponseEntity<String> response =
                this.restTemplate.getForEntity("http://localhost:" + port + "/api/users", String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    @DisplayName("POST /api/auth/register register user")
    public void registerUser() throws Exception {
        String firstName = "Janko";
        String lastName = "Bananko";
        String email = "janko.bananko@gmail.com";

        UserEntity userEntity = registerUser(firstName, lastName, email);

        log.info("UserEntity: {}", userEntity);
        // Checking that the register response returns the right parameter of the right user
        assertThat(userEntity.getFirstName()).isEqualTo(firstName);
        assertThat(userEntity.getLastName()).isEqualTo(lastName);
        assertThat(userEntity.getEmail()).isEqualTo(email);
    }

    private UserEntity registerUser(String firstName, String lastName, String email) throws JSONException {
        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registration parameters of the user
        JSONObject registerParameters = new JSONObject();
        registerParameters.put("firstName", firstName);
        registerParameters.put("lastName", lastName);
        registerParameters.put("email", email);
        registerParameters.put("password", "Password90!");
        registerParameters.put("secondPassword", "Password90!");

        HttpEntity<String> registerRequest =
                new HttpEntity<>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse =
                this.restTemplate.exchange("http://localhost:" + port + "/api/auth/register", HttpMethod.POST, registerRequest, UserEntity.class);

        return registerResponse.getBody();
    }

    @Test
    @DisplayName("POST /api/auth/register -> /api/auth/login returns ok response")
    public void registrationLoginOk() throws Exception {
        String firstName = "Janko";
        String lastName = "Bananko";
        String email = "janko.bananko1@gmail.com";

        ResponseEntity<UserEntity> loginResponse = registrationLoginOk(firstName, lastName, email);

        // Checking that the log in response returns the right parameter of the right user
        assertThat(Objects.requireNonNull(loginResponse.getBody()).getFirstName()).isEqualTo(firstName);
        assertThat(loginResponse.getBody().getLastName()).isEqualTo(lastName);
        assertThat(loginResponse.getBody().getEmail()).isEqualTo(email);

        // Expected ok since the user has registered an account and login parameters are correct
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<UserEntity> registrationLoginOk(String firstName, String lastName, String email) throws Exception {
        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        registerUser(firstName, lastName, email);

        // Login parameter of the previous registered user
        JSONObject loginParameters = new JSONObject();
        loginParameters.put("email", email);
        loginParameters.put("password", "Password90!");

        HttpEntity<String> loginRequest =
                new HttpEntity<>(loginParameters.toString(), headers);

        // POST request to log in the user
        return this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/login", loginRequest, UserEntity.class);
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
                new HttpEntity<>(loginParameters.toString(), headers);

        // Resource Access Exception is expected since the user does not exist
        assertThrows(ResourceAccessException.class,
                () -> {
                    // POST request to log in the user
                    this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/login",
                            loginRequest, UserEntity.class);
                });

    }

    @Test
    @DisplayName("POST /api/auth/register returns bad request if the password/email constraints are not respected")
    public void wrongRegisterTest() throws Exception {

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
                new HttpEntity<>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse =
                this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", registerRequest, UserEntity.class);

        // Expected Bad Request since the user has not respected password constraints
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    public void logoutWithoutLogin() {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to logout
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<>("", headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    public void logoutWithoutLogin1() throws Exception {

        // Register user without login
        this.registerUser("Bob", "Miller", "bob.miller@gmail.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to logout
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<>("", headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("DELETE /api/users/id register, login, delete user")
    public void deleteUserCheck() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestC", "TestC", "testc.testc@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // DELETE user
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/" + userID, HttpMethod.DELETE, new HttpEntity<>("", headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/users register, login, logout, get users unauthorized")
    public void getUsersUnauthorized() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestA", "TestA", "testa.testa@gmail.com");

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            logoutHeaders.add("Cookie", loginCookie);

        // POST to logout
        ResponseEntity<String> response = this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<>("", logoutHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders getUsersHeaders = new HttpHeaders();
        getUsersHeaders.setContentType(MediaType.APPLICATION_JSON);
        // GET users
        response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users", HttpMethod.GET, new HttpEntity<>("", getUsersHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("PUT /api/users/id register, login, put user")
    public void patchUserCheck() throws Exception {

        // // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestB", "TestB", "testb.testb@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        JSONObject updateParams = new JSONObject();
        updateParams.put("firstName", "Lorenzo");
        updateParams.put("lastName", "Poletti");
        updateParams.put("email", "lorenzo.poletti@gmail.com");

        // Update user information
        ResponseEntity<UserEntity> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/" + userID, HttpMethod.PUT, new HttpEntity<>(updateParams.toString(), headers), UserEntity.class);

        assertThat(Objects.requireNonNull(response.getBody()).getFirstName()).isEqualTo(updateParams.get("firstName"));
        assertThat(response.getBody().getLastName()).isEqualTo(updateParams.get("lastName"));
        assertThat(response.getBody().getEmail()).isEqualTo(updateParams.get("email"));
    }

    @Test
    @DisplayName("GET /api/users registers n users and check the list")
    public void getUsers() throws Exception {

        // Generate and register users
        int registeredUserNumber = 50;
        List<List<String>> users = Lists.newArrayList();
        for (int i = 0; i < registeredUserNumber; i++)
            users.add(Lists.newArrayList("Firstname" + (char) ((i % 25) + 97), "Lastname" + (char) ((i % 25) + 97), "firstname" + i + ".lastname@gmail.com"));

        // Register users
        for (int i = 0; i < users.size() - 1; i++)
            this.registerUser(users.get(i).get(0), users.get(i).get(1), users.get(i).get(2));

        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk(users.get(users.size() - 1).get(0), users.get(users.size() - 1).get(1), users.get(users.size() - 1).get(2));

        // Headers of the request
        HttpHeaders getUsersHeaders = new HttpHeaders();
        getUsersHeaders.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            getUsersHeaders.add("Cookie", loginCookie);

        // GET users
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users?size=" + registeredUserNumber + 100, HttpMethod.GET, new HttpEntity<>("", getUsersHeaders), String.class);

        JsonNode array = objectMapper.readTree(response.getBody()).get("content");
        ObjectReader reader = objectMapper.readerFor(new TypeReference<List<UserEntity>>() {
        });
        List<UserEntity> usersList = reader.readValue(array);

        // Check if the users registered are in the list returned by /users

        Map<String, UserEntity> emailUserMap = usersList.stream().collect(Collectors.toMap(UserEntity::getEmail, Function.identity()));

        usersList.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getEmail(), o2.getEmail()));

        for (List<String> user : users) {

            UserEntity userEntity = emailUserMap.get(user.get(2));

            log.info("User: {}, UserEntity: {}", user, userEntity);

            assertThat(userEntity).isNotNull();
            assertThat(userEntity.getFirstName()).isEqualTo(user.get(0));
            assertThat(userEntity.getLastName()).isEqualTo(user.get(1));
        }
    }

    @Test
    @DisplayName("GET /repo/{id} returns the correct information")
    public void getRepoOkTest() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("getRepoTest", "getRepoTest", "getRepoTest@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();


        // Creation of a repo
        Repo repo = new Repo();
        // Repo parameter setting
        repo.setRepoName("RepoName");
        repo.setSecurity(5);
        repo.setAvailability(3);
        repo.setDomain(RepoDomain.FINANCE);
        repo.setType(RepoType.MOBILE);
        repo.setDescription("RepoDescription");
        repo.setUserData(true);
        repo.setFullName("username/repoName");
        repo.setOwner(userID);
        repo.setUrl("www.url.com");
        repo.setHookUrl("www.hook.com");
        repo.setHooksUrl("www.hooks.com");
        repo.setBranchesUrl("www.branches.com");
        repo.setCloneUrl("www.clone.com");
        repo.setHtmlUrl("www.html.com");

        // Repository is saved in database
        repoRepository.save(repo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // DELETE user
        ResponseEntity<Repo> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/repo/1", HttpMethod.GET, new HttpEntity<>("", headers), Repo.class);

        // Response status is ok since the id of the owner is equal to the id of the user who made the request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check for parameters correctness
        assertThat(Objects.requireNonNull(response.getBody()).getRepoName()).isEqualTo("RepoName");
        assertThat(Objects.requireNonNull(response.getBody()).getSecurity()).isEqualTo(5);
        assertThat(Objects.requireNonNull(response.getBody()).getAvailability()).isEqualTo(3);
        assertThat(Objects.requireNonNull(response.getBody()).getDomain()).isEqualTo(RepoDomain.FINANCE);
        assertThat(Objects.requireNonNull(response.getBody()).getType()).isEqualTo(RepoType.MOBILE);
        assertThat(Objects.requireNonNull(response.getBody()).getDescription()).isEqualTo("RepoDescription");
        assertThat(Objects.requireNonNull(response.getBody()).getUserData()).isEqualTo(true);
        assertThat(Objects.requireNonNull(response.getBody()).getFullName()).isEqualTo("username/repoName");
        assertThat(Objects.requireNonNull(response.getBody()).getOwner()).isEqualTo(userID);
        assertThat(Objects.requireNonNull(response.getBody()).getUrl()).isEqualTo("www.url.com");
        assertThat(Objects.requireNonNull(response.getBody()).getHookUrl()).isEqualTo("www.hook.com");
        assertThat(Objects.requireNonNull(response.getBody()).getHooksUrl()).isEqualTo("www.hooks.com");
        assertThat(Objects.requireNonNull(response.getBody()).getBranchesUrl()).isEqualTo("www.branches.com");
        assertThat(Objects.requireNonNull(response.getBody()).getCloneUrl()).isEqualTo("www.clone.com");
        assertThat(Objects.requireNonNull(response.getBody()).getHtmlUrl()).isEqualTo("www.html.com");



    }

}
