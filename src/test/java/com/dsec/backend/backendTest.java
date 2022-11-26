package com.dsec.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class backendTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("/api/users Returns Unauthorized when the user is not logged")
    public void getUsersTest() throws Exception {

        ResponseEntity<String> response =
                this.restTemplate.getForEntity("http://localhost:" + port + "/api/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    @DisplayName("/api/users Returns Unauthorized when the user is not logged")
    public void registrationTest() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject registerParameters = new JSONObject();
        registerParameters.put("firstName", "name");
        registerParameters.put("lastName", "name");
        registerParameters.put("email", "second@gmail.com");
        registerParameters.put("password", "NameName1900!");
        registerParameters.put("secondPassword", "NameName1900!");

        HttpEntity<String> request =
                new HttpEntity<String>(registerParameters.toString(), headers);

        ResponseEntity response =
                this.restTemplate.postForObject("http://localhost:" + port + "/api/auth/register", request, ResponseEntity.class);

        System.out.println(response.getBody());

    }

}
