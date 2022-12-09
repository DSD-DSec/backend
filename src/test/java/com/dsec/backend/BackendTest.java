package com.dsec.backend;

import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// The test profile allow tests to run having db always clean
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @Autowired
    private JobRepository jobRepository;


    @Test
    @DisplayName("GET /api/users returns unauthorized if the user is not logged in")
    @Order(0)
    public void getUsersUnauthorizedTest() {

        // GET request to retrieve all the users
        ResponseEntity<String> response =
                this.restTemplate.getForEntity("http://localhost:" + port + "/api/users", String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    @DisplayName("POST /api/auth/register register user")
    @Order(1)
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
    @Order(2)
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

        registerUser(firstName, lastName, email);

        return loginOk(email);
    }

    private ResponseEntity<UserEntity> loginOk( String email) throws JSONException {
        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
    @Order(3)
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
    @Order(4)
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
    @Order(5)
    public void logoutWithoutLogin() {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to log out
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<>("", headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    @Order(6)
    public void logoutWithoutLogin1() throws Exception {

        // Register user without login
        this.registerUser("Bob", "Miller", "bob.miller@gmail.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to log out
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<>("", headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("DELETE /api/users/id register, login, delete user")
    @Order(7)
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
    @Order(8)
    public void getUsersUnauthorized() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestA", "TestA", "testa.testa@gmail.com");

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            logoutHeaders.add("Cookie", loginCookie);

        // POST to log out
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
    @Order(9)
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
    @Order(10)
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
    @DisplayName("GET /api/repo/{repo}/jobs returns not found if repo doesn't exist")
    @Order(11)
    public void getRepoJobsNotFound() throws Exception {


        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestD", "TestD", "testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        long id = 12345678;
        Repo repo = new Repo(id,"RepoTest","test","test","test","test","test","test",loginResponse.getBody());
        repoRepository.save(repo);

        // GET request to retrieve all jobs from the repo with id+1
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/repo/" + (id+1) + "/jobs", HttpMethod.GET, new HttpEntity<>("", header), String.class);

        // Expected NOT FOUND since the repo with id+1 doesn't exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("GET /api/job/{id} returns not found if job doesn't exist")
    @Order(12)
    public void getJobsNotFound() throws Exception {

        long jobID=123;
        long repoID = 12345678;
        jobRepository.save(new Job(jobID, "Debug string1", repoRepository.getReferenceById(repoID)));

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk( "testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);


        // GET the job infos
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/job/" + (jobID+1), HttpMethod.GET, new HttpEntity<>("", header), String.class);

        // Expected NOT FOUND since the job with id+1 doesn't exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("GET /api/repo/{repo}/jobs returns the right jobs list")
    @Order(13)
    public void getRepoJobs() throws Exception {


        // Create jobs for repo repoID
        long repoID = 12345678;
        List<Job> jobs = new ArrayList<>();

        for(int i=0; i < 20; i++)
        {
            Job jobEntity = new Job("Debug string"+i, repoRepository.getReferenceById(repoID));
            jobRepository.save(jobEntity);
            jobs.add(jobEntity);
        }

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk( "testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);


        // GET jobs of repo repoID
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/repo/" + (repoID) + "/jobs", HttpMethod.GET, new HttpEntity<>("", header), String.class);
        JsonNode array = objectMapper.readTree(response.getBody());
        ObjectReader reader = objectMapper.readerFor(new TypeReference<List<Job>>() {
        });
        List<Job> jobsList = reader.readValue(array);

        // Check if the inserted jobs are present in the repo repoID
        Map<Long, Job> jobListMap = jobsList.stream().collect(Collectors.toMap(Job::getId, Function.identity()));

        for(Job job : jobs)
        {
            Job jobEntity = jobListMap.get(job.getId());
            assertThat(jobEntity).isNotNull();
            assertThat(job.getId()).isEqualTo(jobEntity.getId());
            assertThat(job.getLog()).isEqualTo(jobEntity.getLog());
        }
    }

    @Test
    @DisplayName("GET /api/job/{id} check if the returned job is correct")
    @Order(14)
    public void getJob() throws Exception {

        long repoID = 12345678;

        // Create a job for repo repoID
        Job jobEntity = new Job("Output string test", repoRepository.getReferenceById(repoID));
        jobRepository.save(jobEntity);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk( "testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);


        // GET request to retrieve job info
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/job/" + (jobEntity.getId()), HttpMethod.GET, new HttpEntity<>("", header), String.class);

        JsonNode array = objectMapper.readTree(response.getBody());
        ObjectReader reader = objectMapper.readerFor(new TypeReference<Job>() {
        });
        Job job = reader.readValue(array);

        // Check if the inserted job infos are equal to the one obtained with the endpoint
        assertThat(job.getId()).isEqualTo(jobEntity.getId());
        assertThat(job.getLog()).isEqualTo(jobEntity.getLog());
    }

    @Test
    @DisplayName("GET /api/repo/{repo}/jobs returns unauthorized if the user doesn't belong to the repo")
    @Order(15)
    public void getRepoJobsUnauthorized() throws Exception {


        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk( "testa.testa@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        long id = 12345678;

        // GET request to retrieve all jobs from the repo with id
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/repo/" + (id) + "/jobs", HttpMethod.GET, new HttpEntity<>("", header), String.class);

        // Expected UNAUTHORIZED since the repo doesn't belong to the login user
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    @DisplayName("GET /api/job/{id} returns unauthorized if job's repo doesn't belong to the user")
    @Order(16)
    public void getJobsUnauthorized() throws Exception {

        long jobID=1;

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk( "testa.testa@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);


        // GET the job infos
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/job/" + jobID, HttpMethod.GET, new HttpEntity<>("", header), String.class);

        // Expected UNAUTHORIZED since the job's repo doesn't belong to the user
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

}
