package com.dsec.backend.service.github;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.dsec.backend.config.ConfigProperties;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.model.github.CreateWebhook;
import com.dsec.backend.model.github.GetWebhookDTO;
import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.model.github.UrlDTO;
import com.dsec.backend.model.github.UserDTO;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.service.user.UserService;

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

    private static final String BEARER = "Bearer ";

    private final WebClient webClient;
    private final UserService userService;
    private final ConfigProperties configProperties;
    private final RepoRepository repoRepository;

    public GithubClientServiceImpl(UserService userService, ConfigProperties configProperties,
            RepoRepository repoRepository) {

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
        this.configProperties = configProperties;
        this.repoRepository = repoRepository;
    }

    @Override
    public List<RepoDTO> getRepos(Jwt jwt) {
        List<RepoDTO> list = get("/user/repos", userService.getToken(jwt))
                .bodyToMono(new ParameterizedTypeReference<List<RepoDTO>>() {
                }).block();

        List<Repo> repos = repoRepository.findAll();
        Set<String> repoFullNames = repos.stream().map(Repo::getFullName).collect(Collectors.toSet());
        Set<Long> githubIds = repos.stream().map(Repo::getGithubId).collect(Collectors.toSet());

        return list.stream().filter(r -> (!githubIds.contains(r.getId()) && !repoFullNames.contains(r.getFullName())))
                .toList();

    }

    @Override
    public Mono<UserDTO> getUser(Jwt jwt) {
        return get("/user", userService.getToken(jwt)).bodyToMono(UserDTO.class).map(this::validate);
    }

    @Override
    public Mono<RepoDTO> getRepo(String fullRepoName, Jwt jwt) {
        return get("/repos/" + fullRepoName, userService.getToken(jwt)).bodyToMono(RepoDTO.class)
                .map(this::validate);
    }

    @Override
    public Mono<String> createWebHook(String fullRepoName, Jwt jwt) {
        String token = userService.getToken(jwt);

        CreateWebhook createWebhook = CreateWebhook.builder().name("web").active(true)
                .events(List.of("push", "pull_request"))
                .config(Map.of("url", configProperties.getBackend().getUrl() + "/api/github/webhook", "content_type",
                        "json", "insecure_ssl", "0"))
                .build();

        return webClient.post().uri("/repos/" + fullRepoName + "/hooks")
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .body(Mono.just(createWebhook), CreateWebhook.class)
                .retrieve().bodyToMono(UrlDTO.class).map(this::validate).map(UrlDTO::getUrl);
    }

    @Override
    public void triggerHook(String hookUrl, Jwt jwt) {
        String token = userService.getToken(jwt);
        webClient.post().uri(hookUrl + "/tests")
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve().toBodilessEntity().subscribe(e -> log.info("Test webhook {}", e));
    }

    @Override
    public void deleteWebhook(String hookUrl, Jwt jwt) {
        String token = userService.getToken(jwt);

        webClient.delete().uri(hookUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve().toBodilessEntity().doOnError(error -> log.error("error occurred while reading body", error))
                .doOnCancel(() -> log.error("Get request is cancelled"))
                .subscribe(e -> log.info("Delete webhook {}", e));

    }

    @Override
    public Mono<List<GetWebhookDTO>> getWebhooks(String fullRepoName, Jwt jwt) {
        return get("/repos/" + fullRepoName + "/hooks", userService.getToken(jwt))
                .bodyToMono(new ParameterizedTypeReference<List<GetWebhookDTO>>() {
                });
    }

    @Override
    public Optional<GetWebhookDTO> getExistingHook(List<GetWebhookDTO> list) {
        return list.stream().filter(h -> {
            String url = h.getConfig().get("url");

            return url.matches(configProperties.getBackend().getUrl() + "/api/github/webhook");
        }).findAny();
    }

    private ResponseSpec get(String path, String token) {
        return webClient.get().uri(path).header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve();
    }

    private <T> T validate(@Valid T t) {
        return t;
    }

}
