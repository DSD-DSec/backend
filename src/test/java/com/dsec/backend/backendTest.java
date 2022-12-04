package com.dsec.backend;

import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.model.user.UserUpdateDTO;
import com.dsec.backend.repository.UserRepository;
import com.dsec.backend.service.UserService;
import com.dsec.backend.service.UserServiceImpl;
import io.swagger.v3.core.util.Json;
import org.assertj.core.util.Lists;
import org.h2.engine.User;
import org.json.JSONArray;
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

import java.util.Iterator;
import java.util.List;

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

    private String loginCookie = null;



    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

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
    @DisplayName("POST /api/auth/register register user")
    public long registerUser(String firstName, String lastName, String email) throws Exception {
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
                new HttpEntity<String>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse =
                this.restTemplate.exchange("http://localhost:" + port + "/api/auth/register",HttpMethod.POST, registerRequest,UserEntity.class);

        System.out.println(registerResponse.getBody());
        // Checking that the register response returns the right parameter of the right user
        assertThat(registerResponse.getBody().getFirstName()).isEqualTo(firstName);
        assertThat(registerResponse.getBody().getLastName()).isEqualTo(lastName);
        assertThat(registerResponse.getBody().getEmail()).isEqualTo(email);

        return registerResponse.getBody().getId();
    }

    @Test
    @DisplayName("POST /api/auth/register -> /api/auth/login returns ok response")
    public long registrationLoginOkTest(String firstName, String lastName, String email) throws Exception {
        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        long userID = registerUser(firstName,lastName,email);
        // Login parameter of the previous registered user
        JSONObject loginParameters = new JSONObject();
        loginParameters.put("email", email);
        loginParameters.put("password", "Password90!");

        HttpEntity<String> loginRequest =
                new HttpEntity<String>(loginParameters.toString(), headers);

        // POST request to log in the user
        ResponseEntity<UserEntity> loginResponse =
                this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/login", loginRequest, UserEntity.class);

        loginCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        // Checking that the log in response returns the right parameter of the right user
        assertThat(loginResponse.getBody().getFirstName()).isEqualTo(firstName);
        assertThat(loginResponse.getBody().getLastName()).isEqualTo(lastName);
        assertThat(loginResponse.getBody().getEmail()).isEqualTo(email);

        // Expected ok since the user has registered an account and login parameters are correct
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        return userID;
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
                new HttpEntity<String>(registerParameters.toString(), headers);

        // POST request to register the user
        ResponseEntity<UserEntity> registerResponse =
                this.restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", registerRequest, UserEntity.class);

        // Expected Bad Request since the user has not respected password constraints
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    public void logoutWithoutLogin() throws Exception {

        // Headers of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to logout
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<String>("",headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/logout returns unauthorized if user is not logged in")
    public void logoutWithoutLogin1() throws Exception {

        // Register user without login
        this.registerUser("Bob","Miller","bob.miller@gmail.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST to logout
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<String>("",headers), String.class);

        // Expected Unauthorized since the user is not logged in
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("DELETE /api/users/id register, login, delete user")
    public void deleteUserCheck() throws Exception {

        // Register user and login
        long userID = this.registrationLoginOkTest("TestC","TestC","testc.testc@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(loginCookie != null)
            headers.add("Cookie",loginCookie);

        // DELETE user
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/"+userID, HttpMethod.DELETE, new HttpEntity<String>("",headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/users register, login, logout, get users unauthorized")
    public void getUsersUnauthorized() throws Exception {

        // Register user and login
        this.registrationLoginOkTest("TestA","TestA","testa.testa@gmail.com");

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        if(loginCookie != null)
            logoutHeaders.add("Cookie",loginCookie);

        // POST to logout
        ResponseEntity<String> response = this.restTemplate.exchange("http://localhost:" + port + "/api/users/logout", HttpMethod.POST, new HttpEntity<String>("",logoutHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders getUsersHeaders = new HttpHeaders();
        getUsersHeaders.setContentType(MediaType.APPLICATION_JSON);
        // GET users
        response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users", HttpMethod.GET, new HttpEntity<String>("",getUsersHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("PUT /api/users/id register, login, put user")
    public void patchUserCheck() throws Exception {

        // // Register user and login
        long userID = this.registrationLoginOkTest("TestB","TestB","testb.testb@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(loginCookie != null)
            headers.add("Cookie",loginCookie);

        JSONObject updateParams = new JSONObject();
        updateParams.put("firstName", "Lorenzo");
        updateParams.put("lastName", "Poletti");
        updateParams.put("email", "lorenzo.poletti@gmail.com");

        // Update user information
        ResponseEntity<UserEntity> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users/"+userID, HttpMethod.PUT, new HttpEntity<String>(updateParams.toString(),headers), UserEntity.class);

        assertThat(response.getBody().getFirstName()).isEqualTo(updateParams.get("firstName"));
        assertThat(response.getBody().getLastName()).isEqualTo(updateParams.get("lastName"));
        assertThat(response.getBody().getEmail()).isEqualTo(updateParams.get("email"));
    }

    @Test
    @DisplayName("GET /api/users registers n users and check the list")
    public void getUsers() throws Exception {

        // Generate and register users
        int registeredUserNumber = 50;
        List<List<String>> users = Lists.newArrayList();
        for(int i = 0; i < registeredUserNumber; i++)
            users.add(Lists.newArrayList("Firstname"+(char)((i%25)+97),"Lastname"+(char)((i%25)+97),"firstname"+i+".lastname@gmail.com"));

        // Register users
        for(int i=0; i < users.size()-1;i++)
            this.registerUser(users.get(i).get(0),users.get(i).get(1),users.get(i).get(2));

        this.registrationLoginOkTest(users.get(users.size()-1).get(0),users.get(users.size()-1).get(1),users.get(users.size()-1).get(2));


        // Headers of the request
        HttpHeaders getUsersHeaders = new HttpHeaders();
        getUsersHeaders.setContentType(MediaType.APPLICATION_JSON);
        if(loginCookie != null)
            getUsersHeaders.add("Cookie",loginCookie);

        // GET users
        ResponseEntity<String> response =
                this.restTemplate.exchange("http://localhost:" + port + "/api/users?size="+registeredUserNumber+100, HttpMethod.GET, new HttpEntity<String>("",getUsersHeaders), String.class);

        // Check if the users registered are in the list returned by /users
        JSONObject usersJson = new JSONObject(response.getBody());

        boolean userNotPresent = false;

        for(int i = 0; i < users.size();i++)
        {
            System.out.println(users.get(i));
            Iterator<String> keys = usersJson.keys();
            userNotPresent = true;

            while(keys.hasNext() && userNotPresent) {
                String key = keys.next();

                if(usersJson.get(key) instanceof JSONArray)
                {
                    JSONArray ja = usersJson.getJSONArray(key);

                    System.out.println(ja.length());
                    for(int j = 0; j < ja.length();j++)
                    {
                        System.out.println(ja.getJSONObject(j));
                        if(!ja.getJSONObject(j).has("firstName"))
                            break;
                        else
                        {
                            if(users.get(i).get(0).equals(ja.getJSONObject(j).get("firstName")) &&
                                users.get(i).get(1).equals(ja.getJSONObject(j).get("lastName")) &&
                                users.get(i).get(2).equals(ja.getJSONObject(j).get("email")))
                            {
                                userNotPresent = false;
                            }
                        }
                    }
                }
            }
            if(userNotPresent)
                break;
        }



        assertThat(userNotPresent).isEqualTo(false);
    }

}
