package com.dsec.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.model.github.UserDTO;
import com.dsec.backend.model.github.WebhookDTO;
import com.dsec.backend.service.GithubClientService;
import com.dsec.backend.service.WebHookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/github", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GithubController {
    private final GithubClientService githubClientService;
    private final WebHookService webHookService;

    @GetMapping("/user")
    public Mono<UserDTO> getUser(@AuthenticationPrincipal Jwt jwt) {
        return githubClientService.getUser(jwt);
    }

    @GetMapping("/user/repos")
    public Mono<List<RepoDTO>> getRepos(@AuthenticationPrincipal Jwt jwt) {
        return githubClientService.getRepos(jwt);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Object> webhook(@RequestBody WebhookDTO body, @RequestHeader("X-GitHub-Event") String event) {
        log.info("Webhook triggered event: {} payload: {}", event, body);

        if (event.equals("push") || event.equals("pull_request")) {
            webHookService.webhook(body);
        }

        return ResponseEntity.ok().build();
    }

}
