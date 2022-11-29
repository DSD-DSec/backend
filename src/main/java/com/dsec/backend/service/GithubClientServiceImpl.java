package com.dsec.backend.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.model.github.CreateWebhook;
import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.model.github.UrlDTO;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
@Slf4j
public class GithubClientServiceImpl implements GithubClientService {
    private static final String BASE_URL = "https://api.github.com";
    public static final int TIMEOUT = 5000;

    private final WebClient webClient;
    private final UserService userService;

    @Value("${backend.url}")
    private String backendUrl;

    public GithubClientServiceImpl(UserService userService) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .responseTimeout(Duration.ofMillis(TIMEOUT))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder().baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", BASE_URL))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        this.userService = userService;
    }

    @Override
    public Mono<String> getRepos(Jwt jwt) {
        return get("/user/repos", userService.getToken(jwt)).bodyToMono(String.class);
    }

    @Override
    public Mono<String> getUser(Jwt jwt) {
        return get("/user", userService.getToken(jwt)).bodyToMono(String.class);
    }

    @Override
    public Mono<Repo> getRepo(String fullRepoName, Jwt jwt) {
        return get("/repos/" + fullRepoName, userService.getToken(jwt)).bodyToMono(RepoDTO.class)
                .map(dto -> validate(dto)).map(dto -> {
                    Repo repo = new Repo();
                    BeanUtils.copyProperties(dto, repo);
                    return repo;
                });
    }

    @Override
    public Mono<String> createWebHook(String fullRepoName, Jwt jwt) {
        String token = userService.getToken(jwt);

        CreateWebhook createWebhook = CreateWebhook.builder().name("web").active(true)
                .events(List.of("push", "pull_request"))
                .config(Map.of("url", backendUrl + "/api/github/webhook", "content_type", "json", "insecure_ssl", "0"))
                .build();

        return webClient.post().uri("/repos/" + fullRepoName + "/hooks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(Mono.just(createWebhook), CreateWebhook.class)
                .retrieve().bodyToMono(UrlDTO.class).map(dto -> validate(dto)).map(dto -> dto.getUrl());
    }

    @Override
    public void triggerHook(String hookUrl, Jwt jwt) {
        String token = userService.getToken(jwt);
        webClient.post().uri(hookUrl + "/tests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve().toBodilessEntity().subscribe((e) -> log.info("Test webhook {}", e));
    }

    private ResponseSpec get(String path, String token) {
        return webClient.get().uri(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve();
    }

    private RepoDTO validate(@Valid RepoDTO repoDTO) {
        return repoDTO;
    }

    private UrlDTO validate(@Valid UrlDTO dto) {
        return dto;
    }

}
