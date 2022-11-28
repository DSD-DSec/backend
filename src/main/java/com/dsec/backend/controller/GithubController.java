package com.dsec.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Mono<String> getUser(@AuthenticationPrincipal Jwt jwt) {
        return githubClientService.getUser(jwt);
    }

    @GetMapping("/user/repos")
    public Mono<String> getRepos(@AuthenticationPrincipal Jwt jwt) {
        return githubClientService.getRepos(jwt);
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody WebhookDTO body) {
        log.info("Webhook triggered {}", body);

        webHookService.webhook(body);

        return ResponseEntity.ok().build();
    }

}