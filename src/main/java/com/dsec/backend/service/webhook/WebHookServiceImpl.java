package com.dsec.backend.service.webhook;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.dsec.backend.service.async.AsyncService;
import com.dsec.backend.service.repo.RepoService;
import com.dsec.backend.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.model.tools.GitleaksDTO;
import com.dsec.backend.model.github.WebhookDTO;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.util.attrconverter.LocalDateTimeAttributeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
@Slf4j
public class WebHookServiceImpl implements WebHookService {
    private final AsyncService asyncService;
    private final RepoService repoService;
    private final UserService userService;
    private final JobRepository jobRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final String toolUrl;

    public WebHookServiceImpl(AsyncService asyncService, RepoService repoService, UserService userService,
            JobRepository jobRepository, @Value("${tool.url}") String toolUrl, ObjectMapper objectMapper) {
        this.asyncService = asyncService;
        this.repoService = repoService;
        this.userService = userService;
        this.jobRepository = jobRepository;
        this.toolUrl = toolUrl;
        this.objectMapper = objectMapper;

        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofMillis(30000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder().baseUrl(toolUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", toolUrl))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public void webhook(WebhookDTO dto) {
        asyncService.runCommands(() -> {

            log.info("Tool url {}", toolUrl);

            Repo repo = repoService.fetchByGithubId(dto.getRepoDto().getId());

            Job job = Job.builder().startTime(LocalDateTimeAttributeConverter.now()).repo(repo).build();

            job = jobRepository.save(job);

            UserEntity userEntity = repo.getOwner();

            String token = userService.getToken(userEntity);

            String[] urlSplit = repo.getCloneUrl().split("://");

            Map<String, String> map = Map.of("link", urlSplit[0] + "://" + token + "@" + urlSplit[1]);

            String result = null;
            try {
                result = webClient.post().uri("/gitleaks")
                        .body(Mono.just(map), Map.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                List<GitleaksDTO> list = objectMapper.readValue(result,
                        new TypeReference<List<GitleaksDTO>>() {
                        });
                job.setLog(list);
                job.setCompliant(list.isEmpty());

            } catch (final IOException e) {
                log.error("JSON reading error {result}", result, e);
            } catch (final Exception e) {
                log.error("Analysis request error", e);
            }

            job.setEndTime(LocalDateTimeAttributeConverter.now());

            log.info("New job result {}", jobRepository.save(job));
        });
    }

}
