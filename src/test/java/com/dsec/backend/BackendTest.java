package com.dsec.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Language;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.RepoDomain;
import com.dsec.backend.entity.RepoType;
import com.dsec.backend.entity.Tool;
import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.entity.ToolRepo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRepo;
import com.dsec.backend.model.job.JobDTO;
import com.dsec.backend.model.tools.GitleaksDTO;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.repository.ToolRepoRepository;
import com.dsec.backend.repository.ToolRepository;
import com.dsec.backend.repository.UserRepoRepository;
import com.dsec.backend.service.user.UserService;
import com.dsec.backend.util.attrconverter.LocalDateTimeAttributeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepoRepository userRepoRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolRepoRepository toolRepoRepository;

    @Test
    @DisplayName("GET /api/users returns unauthorized if the user is not logged in")
    @Order(0)
    void getUsersUnauthorizedTest() {

        // GET request to retrieve all the users
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/users",
                String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    @DisplayName("POST /api/auth/register register user")
    @Order(1)
    void registerUser() throws Exception {
        String firstName = "Janko";
        String lastName = "Bananko";
        String email = "janko.bananko@gmail.com";

        UserEntity userEntity = registerUser(firstName, lastName, email);

        log.info("UserEntity: {}", userEntity);
        // Checking that the register response returns the right parameter of the right
        // user
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

        HttpEntity<String> registerRequest = new HttpEntity<>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/register", HttpMethod.POST, registerRequest, UserEntity.class);

        return registerResponse.getBody();
    }

    @Test
    @DisplayName("POST /api/auth/register -> /api/auth/login returns ok response")
    @Order(2)
    void registrationLoginOk() throws Exception {
        String firstName = "Janko";
        String lastName = "Bananko";
        String email = "janko.bananko1@gmail.com";

        ResponseEntity<UserEntity> loginResponse = registrationLoginOk(firstName, lastName, email);

        // Checking that the log in response returns the right parameter of the right
        // user
        assertThat(Objects.requireNonNull(loginResponse.getBody()).getFirstName()).isEqualTo(firstName);
        assertThat(loginResponse.getBody().getLastName()).isEqualTo(lastName);
        assertThat(loginResponse.getBody().getEmail()).isEqualTo(email);

        // Expected ok since the user has registered an account and login parameters are
        // correct
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<UserEntity> registrationLoginOk(String firstName, String lastName, String email)
            throws Exception {

        registerUser(firstName, lastName, email);

        return loginOk(email);
    }

    private ResponseEntity<UserEntity> loginOk(String email) throws JSONException {
        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Login parameter of the previous registered user
        JSONObject loginParameters = new JSONObject();
        loginParameters.put("email", email);
        loginParameters.put("password", "Password90!");

        HttpEntity<String> loginRequest = new HttpEntity<>(loginParameters.toString(), headers);

        // POST request to log in the user
        return this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/login", loginRequest,
                UserEntity.class);
    }

    @Test
    @DisplayName("GET /api/auth/login returns an exception if the user does not exist")
    @Order(3)
    void unauthorizedLoginTest() throws Exception {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Login parameter of a non-existing user
        JSONObject loginParameters = new JSONObject();
        loginParameters.put("email", "bob.miller@gmail.com");
        loginParameters.put("password", "pass");

        HttpEntity<String> loginRequest = new HttpEntity<>(loginParameters.toString(), headers);

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
    void wrongRegisterTest() throws Exception {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registration parameters of the user
        JSONObject registerParameters = new JSONObject();
        registerParameters.put("firstName", "Bob");
        registerParameters.put("lastName", "Miller");
        registerParameters.put("email", "bob.miller@gmail.com");
        registerParameters.put("password", "Password90!");
        // Password parameter does not respect constraints
        registerParameters.put("secondPassword", "pass!");

        HttpEntity<String> registerRequest = new HttpEntity<>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse = this.restTemplate
                .postForEntity("http://localhost:" + port + "/api/auth/register", registerRequest, UserEntity.class);

        // Expected Bad Request since the user has not respected password constraints
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    @Order(5)
    void logoutWithoutLogin() {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to log out
        ResponseEntity<String> response = this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout",
                HttpMethod.POST, new HttpEntity<>("", headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    @Order(6)
    void logoutWithoutLogin1() throws Exception {

        // Register user without login
        this.registerUser("Bob", "Miller", "bob.miller@gmail.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to log out
        ResponseEntity<String> response = this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout",
                HttpMethod.POST, new HttpEntity<>("", headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("DELETE /api/users/id register, login, delete user")
    @Order(7)
    void deleteUserCheck() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestC", "TestC", "testc.testc@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // DELETE user
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userID, HttpMethod.DELETE, new HttpEntity<>("", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/users register, login, logout, get users unauthorized")
    @Order(8)
    void getUsersUnauthorized() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestA", "TestA", "testa.testa@gmail.com");

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            logoutHeaders.add("Cookie", loginCookie);

        // POST to log out
        ResponseEntity<String> response = this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout",
                HttpMethod.POST, new HttpEntity<>("", logoutHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders getUsersHeaders = new HttpHeaders();
        getUsersHeaders.setContentType(MediaType.APPLICATION_JSON);
        // GET users
        response = this.restTemplate.exchange("http://localhost:" + port + "/api/users", HttpMethod.GET,
                new HttpEntity<>("", getUsersHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("PUT /api/users/id register, login, put user")
    @Order(9)
    void patchUserCheck() throws Exception {

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
        ResponseEntity<UserEntity> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userID, HttpMethod.PUT,
                new HttpEntity<>(updateParams.toString(), headers), UserEntity.class);

        assertThat(Objects.requireNonNull(response.getBody()).getFirstName()).isEqualTo(updateParams.get("firstName"));
        assertThat(response.getBody().getLastName()).isEqualTo(updateParams.get("lastName"));
        assertThat(response.getBody().getEmail()).isEqualTo(updateParams.get("email"));
    }

    @Test
    @DisplayName("GET /api/users registers n users and check the list")
    @Order(10)
    void getUsers() throws Exception {

        // Generate and register users
        int registeredUserNumber = 50;
        List<List<String>> users = Lists.newArrayList();
        for (int i = 0; i < registeredUserNumber; i++)
            users.add(Lists.newArrayList("Firstname" + (char) ((i % 25) + 97), "Lastname" + (char) ((i % 25) + 97),
                    "firstname" + i + ".lastname@gmail.com"));

        // Register users
        for (int i = 0; i < users.size() - 1; i++)
            this.registerUser(users.get(i).get(0), users.get(i).get(1), users.get(i).get(2));

        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk(users.get(users.size() - 1).get(0),
                users.get(users.size() - 1).get(1), users.get(users.size() - 1).get(2));

        // Headers of the request
        HttpHeaders getUsersHeaders = new HttpHeaders();
        getUsersHeaders.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            getUsersHeaders.add("Cookie", loginCookie);

        // GET users
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/users?size=" + registeredUserNumber + 100, HttpMethod.GET,
                new HttpEntity<>("", getUsersHeaders), String.class);

        JsonNode array = objectMapper.readTree(response.getBody()).get("content");
        ObjectReader reader = objectMapper.readerFor(new TypeReference<List<UserEntity>>() {
        });
        List<UserEntity> usersList = reader.readValue(array);

        // Check if the users registered are in the list returned by /users

        Map<String, UserEntity> emailUserMap = usersList.stream()
                .collect(Collectors.toMap(UserEntity::getEmail, Function.identity()));

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
    void getRepoJobsNotFound() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("TestD", "TestD", "testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        UserEntity user = userService.fetch(Objects.requireNonNull(loginResponse.getBody()).getId());

        Repo repo = mockRepo("RepoTest");

        repo.setGithubId(1L);
        repo = repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        // GET request to retrieve all jobs from the repo with id+1
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + 1 + "/jobs", HttpMethod.GET,
                new HttpEntity<>("", header), String.class);

        // Expected NOT FOUND since the repo with id+1 doesn't exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("GET /api/job/{id} returns not found if job doesn't exist")
    @Order(12)
    void getJobsNotFound() throws Exception {

        long jobID = 123;
        String repoFullName = "RepoTest";

        Repo repo = repoRepository.findByFullName(repoFullName);
        ToolEntity tool = toolRepository.findByToolName(Tool.GITLEAKS);

        Job job = Job.builder().id(jobID).startTime(LocalDateTimeAttributeConverter.now()).log(mockGitleaksLog)
                .repo(repo).tool(tool).build();

        jobRepository.save(job);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        // GET the job infos
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/job/" + 99999, HttpMethod.GET, new HttpEntity<>("", header),
                String.class);

        // Expected NOT FOUND since the job with id+1 doesn't exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("GET /api/repo/{repo}/jobs returns the right jobs list")
    @Order(13)
    void getRepoJobs() throws Exception {

        // Create jobs for repo with fullName
        String repoFullName = "RepoTest";
        Repo repo = repoRepository.findByFullName(repoFullName);

        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < 20; i++) {

            ToolEntity tool = toolRepository.findByToolName(Tool.GITLEAKS);

            Job jobEntity = Job.builder().log(mockGitleaksLog)
                    .startTime(LocalDateTimeAttributeConverter.now().plusSeconds(i))
                    .repo(repo).tool(tool).build();

            jobEntity = jobRepository.save(jobEntity);
            jobs.add(jobEntity);
        }

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);/*  */

        // GET jobs of repo
        ResponseEntity<List<JobDTO<?>>> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + (repo.getId()) + "/jobs", HttpMethod.GET,
                new HttpEntity<>("", header), new ParameterizedTypeReference<>() {
                });

        log.info("Response = {}", response);

        // if the inserted jobs are present in the repo
        Map<Long, JobDTO<?>> jobListMap = Objects.requireNonNull(response.getBody()).stream()
                .collect(Collectors.toMap(JobDTO::getId, Function.identity()));

        for (Job job : jobs) {
            JobDTO<?> jobEntity = jobListMap.get(job.getId());
            assertThat(jobEntity).isNotNull();
            assertThat(job.getId()).isEqualTo(jobEntity.getId());
            assertThat(job.getStartTime().getSecond()).isEqualTo(jobEntity.getStartTime().getSecond());
        }
    }

    @Test
    @DisplayName("GET /api/job/{id} check if the returned job is correct")
    @Order(14)
    void getJob() throws Exception {

        // Create a job for repo
        String repoFullName = "RepoTest";
        Repo repo = repoRepository.findByFullName(repoFullName);

        ToolEntity tool = toolRepository.findByToolName(Tool.GITLEAKS);

        Job jobEntity = Job.builder().log(mockGitleaksLog)
                .startTime(LocalDateTimeAttributeConverter.now())
                .repo(repo).tool(tool).build();

        jobRepository.save(jobEntity);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        // GET request to retrieve job info
        ResponseEntity<JobDTO<?>> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/job/" + (jobEntity.getId()), HttpMethod.GET,
                new HttpEntity<>("", header), new ParameterizedTypeReference<>() {
                });

        JobDTO<?> job = response.getBody();
        // Check if the inserted job infos are equal to the one obtained with the
        // endpoint
        assert job != null;
        assertThat(job.getId()).isEqualTo(jobEntity.getId());
        assertThat(job.getStartTime().getSecond()).isEqualTo(jobEntity.getStartTime().getSecond());
    }

    @Test
    @DisplayName("GET /api/repo/{repo}/jobs returns forbidden if the user doesn't belong to the repo")
    @Order(15)
    void getRepoJobsForbidden() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testa.testa@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        String repoFullName = "RepoTest";
        Repo repo = repoRepository.findByFullName(repoFullName);

        // GET request to retrieve all jobs from the repo with id
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + (repo.getId()) + "/jobs", HttpMethod.GET,
                new HttpEntity<>("", header), String.class);

        // Expected UNAUTHORIZED since the repo doesn't belong to the login user
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @DisplayName("GET /api/job/{id} returns forbidden if job's repo doesn't belong to the user")
    @Order(16)
    void getJobsForbidden() throws Exception {

        long jobID = 1;

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testa.testa@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        // GET the job infos
        ResponseEntity<String> response = this.restTemplate.exchange("http://localhost:" + port + "/api/job/" + jobID,
                HttpMethod.GET, new HttpEntity<>("", header), String.class);

        // Expected UNAUTHORIZED since the job's repo doesn't belong to the user
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @DisplayName("GET /repo/{id} returns the correct information")
    void getRepoOkTest() throws Exception {

        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("getRepoTest", "getRepoTest",
                "getRepoTest@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        UserEntity user = userService.fetch(userID);

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // Creation of a repo
        Repo repo = mockRepo("");
        // Repo parameter setting
        repo.setRepoName("RepoName");
        repo.setSecurity(5);
        repo.setPrivacy(3);
        repo.setDomain(RepoDomain.FINANCE);
        repo.setType(RepoType.MOBILE);
        repo.setDescription("RepoDescription");
        repo.setUserData(true);
        repo.setFullName("username/repoName");
        repo.setUrl("www.url.com");
        repo.setHookUrl("www.hook.com");
        repo.setHooksUrl("www.hooks.com");
        repo.setBranchesUrl("www.branches.com");
        repo.setCloneUrl("www.clone.com");
        repo.setHtmlUrl("www.html.com");
        repo.setGithubId(6L);

        // Repository is saved in database
        repo = repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        // GET repository
        ResponseEntity<Repo> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId(),
                HttpMethod.GET, new HttpEntity<>("", headers), Repo.class);

        // Response status is ok since the id of the owner is equal to the id of the
        // user who made the request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check for parameters correctness
        assertThat(Objects.requireNonNull(response.getBody()).getRepoName()).isEqualTo("RepoName");
        assertThat(Objects.requireNonNull(response.getBody()).getSecurity()).isEqualTo(5);
        assertThat(Objects.requireNonNull(response.getBody()).getPrivacy()).isEqualTo(3);
        assertThat(Objects.requireNonNull(response.getBody()).getDomain()).isEqualTo(RepoDomain.FINANCE);
        assertThat(Objects.requireNonNull(response.getBody()).getType()).isEqualTo(RepoType.MOBILE);
        assertThat(Objects.requireNonNull(response.getBody()).getDescription()).isEqualTo("RepoDescription");
        assertThat(Objects.requireNonNull(response.getBody()).getUserData()).isTrue();
        assertThat(Objects.requireNonNull(response.getBody()).getFullName()).isEqualTo("username/repoName");
        // to do: fetch users repos to get this information
        // assertThat(Objects.requireNonNull(response.getBody()).getOwner().getId()).isEqualTo(userID);
        assertThat(Objects.requireNonNull(response.getBody()).getUrl()).isEqualTo("www.url.com");
        assertThat(Objects.requireNonNull(response.getBody()).getHookUrl()).isEqualTo("www.hook.com");
        assertThat(Objects.requireNonNull(response.getBody()).getHooksUrl()).isEqualTo("www.hooks.com");
        assertThat(Objects.requireNonNull(response.getBody()).getBranchesUrl()).isEqualTo("www.branches.com");
        assertThat(Objects.requireNonNull(response.getBody()).getCloneUrl()).isEqualTo("www.clone.com");
        assertThat(Objects.requireNonNull(response.getBody()).getHtmlUrl()).isEqualTo("www.html.com");

    }

    @Test
    @DisplayName("POST repo/{id} -> POST repo/{id} -> GET repo/{id} : returns correct information")
    void updateRepoOkTest() throws Exception {
        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("updateRepoOkTest",
                "updateRepoOkTest", "updateRepoOkTest@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        UserEntity user = userService.fetch(userID);

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // Creation of a repo
        Repo repo = mockRepo("");
        // Repo parameter setting
        repo.setRepoName("Repository Name4");
        repo.setSecurity(5);
        repo.setPrivacy(3);
        repo.setDomain(RepoDomain.FINANCE);
        repo.setType(RepoType.MOBILE);
        repo.setDescription("RepoDescription");
        repo.setUserData(true);
        repo.setFullName("username/repo_name3");
        repo.setUrl("www.url.com");
        repo.setHookUrl("www.hook.com");
        repo.setHooksUrl("www.hooks.com");
        repo.setBranchesUrl("www.branches.com");
        repo.setCloneUrl("www.clone.com");
        repo.setHtmlUrl("www.html.com");
        repo.setGithubId(5L);

        // Repository is saved in database
        repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        // Repo update parameters
        JSONObject repoUpdateParameters = new JSONObject();
        repoUpdateParameters.put("repoName", "NewName2");
        repoUpdateParameters.put("description", "New description");
        repoUpdateParameters.put("type", "WEBSITE");
        repoUpdateParameters.put("domain", "SPORT");
        repoUpdateParameters.put("userData", false);
        repoUpdateParameters.put("security", 2);
        repoUpdateParameters.put("privacy", 2);

        HttpEntity<String> updateRepoRequest = new HttpEntity<>(repoUpdateParameters.toString(), headers);

        Long repo_id = repoRepository.findByFullName("username/repo_name3").getId();

        // PUT to update repository information
        ResponseEntity<Repo> updateResponse = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo_id, HttpMethod.PUT, updateRepoRequest, Repo.class);

        // Expected OK since updated is done successfully by the repo owner
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check for parameters correctness
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getRepoName()).isEqualTo("NewName2");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getSecurity()).isEqualTo(2);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getPrivacy()).isEqualTo(2);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getDomain()).isEqualTo(RepoDomain.SPORT);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getType()).isEqualTo(RepoType.WEBSITE);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getDescription()).isEqualTo("New description");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getUserData()).isFalse();
        // to do: fetch users repos to get this information
        // assertThat(Objects.requireNonNull(updateResponse.getBody()).getOwner().getId()).isEqualTo(userID);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getUrl()).isEqualTo("www.url.com");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getHookUrl()).isEqualTo("www.hook.com");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getHooksUrl()).isEqualTo("www.hooks.com");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getBranchesUrl()).isEqualTo("www.branches.com");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getCloneUrl()).isEqualTo("www.clone.com");
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getHtmlUrl()).isEqualTo("www.html.com");

    }

    @Test
    @DisplayName("POST repo/{id} -> POST repo/{id} -> GET repo/{id} : returns correct information")
    void updateRepoBadTest() throws Exception {
        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("updateRepoBadTest",
                "updateRepoBadTest", "updateRepoBadTest@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        UserEntity user = userService.fetch(userID);

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // Creation of a repo
        Repo repo = mockRepo("");
        // Repo parameter setting
        repo.setRepoName("Repository Name");
        repo.setSecurity(5);
        repo.setPrivacy(3);
        repo.setDomain(RepoDomain.FINANCE);
        repo.setType(RepoType.MOBILE);
        repo.setDescription("RepoDescription");
        repo.setUserData(true);
        repo.setFullName("username/repo_name");
        repo.setUrl("www.url.com");
        repo.setHookUrl("www.hook.com");
        repo.setHooksUrl("www.hooks.com");
        repo.setBranchesUrl("www.branches.com");
        repo.setCloneUrl("www.clone.com");
        repo.setHtmlUrl("www.html.com");
        repo.setGithubId(4L);

        // Repository is saved in database
        repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        // Repo update parameters
        JSONObject repoUpdateParameters = new JSONObject();
        repoUpdateParameters.put("full_name", "username/repo_name");
        // Empty repo name, not allowed
        repoUpdateParameters.put("repo_name", "");
        repoUpdateParameters.put("description", "New description");
        // Not a valid option
        repoUpdateParameters.put("type", "FOOTBALL");
        // Not a valid option
        repoUpdateParameters.put("domain", "SPAIN");
        repoUpdateParameters.put("user_data", false);
        repoUpdateParameters.put("security", 2);
        repoUpdateParameters.put("privacy", 2);

        HttpEntity<String> updateRepoRequest = new HttpEntity<>(repoUpdateParameters.toString(), headers);

        Long repo_id = repoRepository.findByFullName("username/repo_name").getId();

        // PUT to update repository information
        ResponseEntity<Repo> updateResponse = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo_id, HttpMethod.PUT, updateRepoRequest, Repo.class);

        // Expected BAD REQUEST since repository name is empty and some parameters
        // option does not exist
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // GET repository to retrieve the initial repo
        ResponseEntity<Repo> getResponse = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo_id, HttpMethod.GET, new HttpEntity<>("", headers),
                Repo.class);

        // Check that parameters have not changed after the insertion
        assertThat(Objects.requireNonNull(getResponse.getBody()).getRepoName()).isEqualTo("Repository Name");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getSecurity()).isEqualTo(5);
        assertThat(Objects.requireNonNull(getResponse.getBody()).getPrivacy()).isEqualTo(3);
        assertThat(Objects.requireNonNull(getResponse.getBody()).getDomain()).isEqualTo(RepoDomain.FINANCE);
        assertThat(Objects.requireNonNull(getResponse.getBody()).getType()).isEqualTo(RepoType.MOBILE);
        assertThat(Objects.requireNonNull(getResponse.getBody()).getDescription()).isEqualTo("RepoDescription");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getUserData()).isTrue();
        assertThat(Objects.requireNonNull(getResponse.getBody()).getFullName()).isEqualTo("username/repo_name");
        // to do: fetch users repos to get this information
        // assertThat(Objects.requireNonNull(getResponse.getBody()).getOwner().getId()).isEqualTo(userID);
        assertThat(Objects.requireNonNull(getResponse.getBody()).getUrl()).isEqualTo("www.url.com");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getHookUrl()).isEqualTo("www.hook.com");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getHooksUrl()).isEqualTo("www.hooks.com");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getBranchesUrl()).isEqualTo("www.branches.com");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getCloneUrl()).isEqualTo("www.clone.com");
        assertThat(Objects.requireNonNull(getResponse.getBody()).getHtmlUrl()).isEqualTo("www.html.com");
    }

    @Test
    @DisplayName("POST repo/{id} -> DELETE repo/{id} -> Valid deletion")
    void deleteRepoOk() throws Exception {
        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("updateRepoBadTest",
                "updateRepoBadTest", "updateRepoBadTest@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        long userID = Objects.requireNonNull(loginResponse.getBody()).getId();

        UserEntity user = userService.fetch(userID);

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // Creation of a repo
        Repo repo = mockRepo("");
        // Repo parameter setting
        repo.setRepoName("Repository Name3");
        repo.setSecurity(5);
        repo.setPrivacy(3);
        repo.setDomain(RepoDomain.FINANCE);
        repo.setType(RepoType.MOBILE);
        repo.setDescription("RepoDescription");
        repo.setUserData(true);
        repo.setFullName("username/repo_name2");
        repo.setUrl("www.url.com");
        repo.setHookUrl("www.hook.com");
        repo.setHooksUrl("www.hooks.com");
        repo.setBranchesUrl("www.branches.com");
        repo.setCloneUrl("www.clone.com");
        repo.setHtmlUrl("www.html.com");
        repo.setGithubId(5L);

        // Repository is saved in database
        repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        Long repo_id = repoRepository.findByFullName("username/repo_name2").getId();

        // DELETE repository
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo_id, HttpMethod.DELETE, new HttpEntity<>("", headers),
                String.class);

        // Expected OK since repository has been correctly deleted
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // GET request to retrieve the repo
        ResponseEntity<Repo> r = this.restTemplate.exchange("http://localhost:" + port + "/api/repo/" + repo_id,
                HttpMethod.GET, new HttpEntity<>("", headers), Repo.class);

        // NOT FOUND is expected since the repository has been deleted
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("POST repo/{id} -> DELETE repo/{id} -> Valid deletion")
    void deleteRepoNotFound() throws Exception {
        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("deleteRepoNotExist",
                "deleteRepoNotExist", "deleteRepoNotExist@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        long repo_id = -1L;

        // DELETE repository
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo_id, HttpMethod.DELETE, new HttpEntity<>("", headers),
                String.class);

        // Expected NOT FOUND since repository does not exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("POST repo/{id} -> DELETE repo/{id} -> Valid deletion")
    void updateRepoNotFound() throws Exception {
        // Register user and login
        ResponseEntity<UserEntity> loginResponse = this.registrationLoginOk("deleteRepoNotExist",
                "deleteRepoNotExist", "deleteRepoNotExist@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            headers.add("Cookie", loginCookie);

        // Repo update parameters
        JSONObject repoUpdateParameters = new JSONObject();
        repoUpdateParameters.put("full_name", "username/repo_name");
        // Empty repo name, not allowed
        repoUpdateParameters.put("repo_name", "");
        repoUpdateParameters.put("description", "New description");
        // Not a valid option
        repoUpdateParameters.put("type", "FOOTBALL");
        // Not a valid option
        repoUpdateParameters.put("domain", "SPAIN");
        repoUpdateParameters.put("user_data", false);
        repoUpdateParameters.put("security", 2);
        repoUpdateParameters.put("availability", 2);

        HttpEntity<String> updateRepoRequest = new HttpEntity<>(repoUpdateParameters.toString(), headers);

        long repo_id = -1L;

        // UPDATE repository
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo_id, HttpMethod.PUT, updateRepoRequest, String.class);

        // Expected BAD REQUEST since repository does not exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @DisplayName("GET tool/ : returns unauthorized if the user is not logged in")
    @Order(17)
    void getToolsUnauthorized() {
        // GET tools
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/tool",
                String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("GET tool/ -> Get all the inserted tools")
    @Order(18)
    void getTools() throws Exception {

        List<ToolEntity> tools = toolRepository.findAll();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        // GET tools
        ResponseEntity<List<ToolEntity>> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/tool/", HttpMethod.GET,
                new HttpEntity<>("", header), new ParameterizedTypeReference<>() {
                });

        // Check if the already present tools in the database are retrieved by the
        // endpoint
        Map<Integer, ToolEntity> toolEntityMap = Objects.requireNonNull(response.getBody()).stream()
                .collect(Collectors.toMap(ToolEntity::getId, Function.identity()));

        for (ToolEntity tool : tools) {
            ToolEntity toolEntity = toolEntityMap.get(tool.getId());
            assertThat(toolEntity).isNotNull();
            assertThat(tool.getId()).isEqualTo(toolEntity.getId());
            assertThat(tool.getToolName()).isEqualTo(toolEntity.getToolName());
            assertThat(tool.getSecurity()).isEqualTo(toolEntity.getSecurity());
            assertThat(tool.getPrivacy()).isEqualTo(toolEntity.getPrivacy());
            assertThat(tool.getUserData()).isEqualTo(toolEntity.getUserData());
            assertThat(tool.getLanguage()).isEqualTo(toolEntity.getLanguage());
        }
    }

    @Test
    @DisplayName("GET /api/repo/{repo}/tools returns not found if repo doesn't exist")
    @Order(19)
    void getToolsRepoNotExist() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        UserEntity user = userService.fetch(Objects.requireNonNull(loginResponse.getBody()).getId());

        Repo repo = mockRepo("RepoTest1");

        repo.setGithubId(2L);
        repo = repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        // GET request to retrieve all jobs from the repo with id+1
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + 1 + "/tools", HttpMethod.GET,
                new HttpEntity<>("", header), String.class);

        // Expected NOT FOUND since the repo with id+1 doesn't exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("GET /api/repo/{repo}/tools returns forbidden if user doesn't have access to repo")
    @Order(20)
    void getToolsRepoForbidden() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testa.testa@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        UserEntity user = userService.fetch(Objects.requireNonNull(loginResponse.getBody()).getId());

        Repo repo = mockRepo("RepoTest2");

        repo.setGithubId(3L);
        repo = repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        loginResponse = this.loginOk("testd.testd@gmail.com");

        loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        header.clear();
        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        // GET request to retrieve all tools from the repo of user "testa" without
        // permissions
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + "/tools", HttpMethod.GET,
                new HttpEntity<>("", header), String.class);

        // Expected FORBIDDEN since "testd" have not accessed to repo of user "testa"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @DisplayName("GET /api/repo/{repo}/tools returns the correct list of tools")
    @Order(21)
    void getToolsRepo() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        UserEntity user = userService.fetch(Objects.requireNonNull(loginResponse.getBody()).getId());

        Repo repo = mockRepo("RepoTest7");

        repo.setGithubId(7L);
        repo = repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);

        toolRepoRepository.save(new ToolRepo(null, repo, toolRepository.findByToolName(Tool.BANDIT)));

        // GET request to retrieve all tools from the repo
        ResponseEntity<List<ToolEntity>> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + "/tools", HttpMethod.GET,
                new HttpEntity<>("", header), new ParameterizedTypeReference<>() {
                });

        Map<Integer, ToolEntity> toolEntityMap = Objects.requireNonNull(response.getBody()).stream()
                .collect(Collectors.toMap(ToolEntity::getId, Function.identity()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check if the tool correspond to the one previously inserted
        ToolEntity toolEntity = toolEntityMap.get(toolRepository.findByToolName(Tool.BANDIT).getId());
        assertThat(toolEntity).isNotNull();
        assertThat(toolRepository.findByToolName(Tool.BANDIT).getId()).isEqualTo(toolEntity.getId());

    }

    @Test
    @DisplayName("PUT /api/repo/{repo}/tools returns not found if repo doesn't exist")
    @Order(22)
    void putToolsRepoNotExist() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        Repo repo = repoRepository.findByFullName("RepoTest7");
        JSONObject updateParams = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(3);
        updateParams.put("tools", jsonArray);

        // PUT request to update an invalid repo id
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + 1 + "/tools", HttpMethod.PUT,
                new HttpEntity<>(updateParams.toString(), header), String.class);

        // Expected NOT FOUND since the repo with id+1 doesn't exist
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("PUT /api/repo/{repo}/tools returns forbidden if user doesn't have access to repo")
    @Order(23)
    void putToolsRepoForbidden() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testa.testa@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        Repo repo = repoRepository.findByFullName("RepoTest7");
        JSONObject updateParams = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(3);
        updateParams.put("tools", jsonArray);

        // PUT request to update a forbidden repo
        ResponseEntity<String> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + "/tools", HttpMethod.PUT,
                new HttpEntity<>(updateParams.toString(), header), String.class);

        // Expected FORBIDDEN since "testa" doesn't have access to this repo
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @DisplayName("PUT /api/repo/{repo}/tools update the tools successfully")
    @Order(23)
    void putToolsRepo() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<UserEntity> loginResponse = this.loginOk("testd.testd@gmail.com");

        String loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (loginCookie != null)
            header.add("Cookie", loginCookie);

        Repo repo = repoRepository.findByFullName("RepoTest7");
        JSONObject updateParams = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(3);
        updateParams.put("tools", jsonArray);

        // PUT request to update tools of my repo
        ResponseEntity<List<ToolEntity>> response = this.restTemplate.exchange(
                "http://localhost:" + port + "/api/repo/" + repo.getId() + "/tools", HttpMethod.PUT,
                new HttpEntity<>(updateParams.toString(), header), new ParameterizedTypeReference<>() {
                });

        Map<Integer, ToolEntity> toolEntityMap = Objects.requireNonNull(response.getBody()).stream()
                .collect(Collectors.toMap(ToolEntity::getId, Function.identity()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check if the tools has been added
        ToolEntity toolEntity = toolEntityMap.get(toolRepository.findByToolName(Tool.FLAWFINDER).getId());
        assertThat(toolEntity).isNotNull();
        assertThat(toolRepository.findByToolName(Tool.FLAWFINDER).getId()).isEqualTo(toolEntity.getId());

        // Check if the previous tool was removed
        toolEntity = toolEntityMap.get(toolRepository.findByToolName(Tool.BANDIT).getId());
        assertThat(toolEntity).isNull();
    }

    private Repo mockRepo(String fullName) {
        return Repo.builder()
                .description("test")
                .type(RepoType.WEBSITE)
                .domain(RepoDomain.FINANCE)
                .userData(true)
                .security(5)
                .privacy(5)
                .fullName(fullName)
                .repoName(fullName)
                .url("test")
                .htmlUrl("test")
                .hooksUrl("test")
                .hookUrl("test")
                .branchesUrl("test")
                .cloneUrl("test")
                .defaultBranch("master")
                .language(Language.PYTHON)
                .build();

    }

    private static final String mockGitleaksLog = "{\"results\":[{\"commit\":\"\",\"author\":\"\",\"date\":\"\",\"description\":\"GenericAPIKey\",\"email\":\"\",\"endColumn\":1,\"endLine\":10,\"entropy\":3.8452778,\"file\":\"20:21:58.103959/src/main/resources/application-dev.properties\",\"fingerprint\":\"20:21:58.103959/src/main/resources/application-dev.properties:generic-api-key:9\",\"match\":\"password=fe977d092face0a96b415c392a7a1b8b168d9420e5c4c998eb92e493919a4637\",\"message\":\"\",\"ruleID\":\"generic-api-key\",\"secret\":\"fe977d092face0a96b415c392a7a1b8b168d9420e5c4c998eb92e493919a4637\",\"startColumn\":21,\"startLine\":\"9\",\"symlinkFile\":\"\"},{\"commit\":\"\",\"author\":\"\",\"date\":\"\",\"description\":\"GenericAPIKey\",\"email\":\"\",\"endColumn\":1,\"endLine\":13,\"entropy\":4.6875,\"file\":\"20:21:58.103959/src/main/resources/application.properties\",\"fingerprint\":\"20:21:58.103959/src/main/resources/application.properties:generic-api-key:12\",\"match\":\"secret.key=7Ogkwuk9qibXGfQlWgAmvhsHWTPEmMw6\",\"message\":\"\",\"ruleID\":\"generic-api-key\",\"secret\":\"7Ogkwuk9qibXGfQlWgAmvhsHWTPEmMw6\",\"startColumn\":2,\"startLine\":\"12\",\"symlinkFile\":\"\"},{\"commit\":\"\",\"author\":\"\",\"date\":\"\",\"description\":\"GenericAPIKey\",\"email\":\"\",\"endColumn\":72,\"endLine\":2,\"entropy\":4.458103,\"file\":\"20:21:58.103959/src/main/webapp/.env.development\",\"fingerprint\":\"20:21:58.103959/src/main/webapp/.env.development:generic-api-key:2\",\"match\":\"API_KEY=\\\"AIzaSyBb4TypFGBbzhOdFA6-I7SbasuJdzJI-Dg\\\"\",\"message\":\"\",\"ruleID\":\"generic-api-key\",\"secret\":\"AIzaSyBb4TypFGBbzhOdFA6-I7SbasuJdzJI-Dg\",\"startColumn\":24,\"startLine\":\"2\",\"symlinkFile\":\"\"}]}";

    private GitleaksDTO mockGitleaksDTO() {
        try {
            return objectMapper.readValue(
                    mockGitleaksLog,
                    GitleaksDTO.class);

        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

}
