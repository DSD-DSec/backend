package com.dsec.backend.service.webhook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;

import com.dsec.backend.config.ConfigProperties;
import com.dsec.backend.entity.Job;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.Tool;
import com.dsec.backend.entity.ToolRepo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.model.github.WebhookDTO;
import com.dsec.backend.model.tools.BanditDTO;
import com.dsec.backend.model.tools.GitleaksDTO;
import com.dsec.backend.model.tools.GoKartDTO;
import com.dsec.backend.model.tools.ProgPilotDTO;
import com.dsec.backend.model.tools.flawfinder.Sarif210Rtm5;
import com.dsec.backend.repository.JobRepository;
import com.dsec.backend.service.async.AsyncService;
import com.dsec.backend.service.repo.RepoService;
import com.dsec.backend.util.attrconverter.LocalDateTimeAttributeConverter;
import com.dsec.backend.util.attrconverter.LogConverter;
import com.fasterxml.jackson.core.type.TypeReference;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
@Slf4j
public class WebHookServiceImpl implements WebHookService {
    private final AsyncService asyncService;
    private final RepoService repoService;
    private final JobRepository jobRepository;
    private final WebClient webClient;
    private final ConfigProperties configProperties;
    private final LogConverter logConverter;

    private final Map<Tool, BiConsumer<String, Job>> mapOfConsumers;

    public WebHookServiceImpl(AsyncService asyncService, RepoService repoService,
            JobRepository jobRepository, ConfigProperties configProperties, LogConverter logConverter) {
        this.asyncService = asyncService;
        this.repoService = repoService;
        this.jobRepository = jobRepository;
        this.configProperties = configProperties;
        this.logConverter = logConverter;

        this.mapOfConsumers = new EnumMap<>(Tool.class);
        mapOfConsumers.put(Tool.BANDIT, this::consumeBandit);
        mapOfConsumers.put(Tool.GITLEAKS, this::consumeGitleaks);
        mapOfConsumers.put(Tool.GOKART, this::consumeGokart);
        mapOfConsumers.put(Tool.PROGPILOT, this::consumeProgpilot);
        mapOfConsumers.put(Tool.FLAWFINDER, this::consumeFlawfinder);

        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofMillis(30000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public void webhook(WebhookDTO dto) {
        asyncService.runCommands(() -> {

            Repo repo = repoService.fetchByGithubId(dto.getRepoDto().getId());
            UserEntity userEntity = repo.getOwner();

            Map<String, String> map = Map.of(
                    "repo", repo.getFullName(),
                    "token", userEntity.getToken());

            for (ToolRepo tr : repo.getToolRepos()) {
                Job job = Job.builder().startTime(LocalDateTimeAttributeConverter.now()).repo(repo).tool(tr.getTool())
                        .build();

                job = jobRepository.save(job);

                try {
                    PipedOutputStream outputStream = new PipedOutputStream();
                    PipedInputStream inputStream = new PipedInputStream(1024 * 10);
                    inputStream.connect(outputStream);

                    analyse(map, tr, outputStream);

                    Reader decoder = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    StringBuilder sb = new StringBuilder();

                    readLog(decoder, sb);

                    mapOfConsumers.get(tr.getTool().getToolName()).accept(sb.toString(), job);

                } catch (final Exception e) {
                    log.error("Analysis request error", e);
                }

                job.setEndTime(LocalDateTimeAttributeConverter.now());

                job = jobRepository.save(job);
            }
        });
    }

    private void analyse(Map<String, String> map, ToolRepo tr, PipedOutputStream outputStream) {
        Flux<DataBuffer> dataBuffer = webClient.post()
                .uri(configProperties.getTools().get(tr.getTool().getToolName()))
                .body(Mono.just(map), Map.class)
                .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()))
                .doOnError(error -> log.error("error occurred while reading body", error))
                .doFinally(s -> {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .doOnCancel(() -> log.error("Get request is cancelled"));

        DataBufferUtils.write(dataBuffer, outputStream)
                .log("Writing to output buffer").subscribe();
    }

    private void readLog(Reader decoder, StringBuilder sb) throws IOException {
        try (BufferedReader buffered = new BufferedReader(decoder)) {
            String line;

            while ((line = buffered.readLine()) != null) {

                sb.append(line);

            }
        }
    }

    private void consumeBandit(String s, Job j) {
        BanditDTO dto = logConverter.convertToEntityAttribute(s, BanditDTO.class);

        j.setLog(s);
        j.setCompliant(dto.getResults().isEmpty());
    }

    private void consumeGitleaks(String s, Job j) {
        List<GitleaksDTO.Result> list = logConverter.convertToEntityAttribute(s,
                new TypeReference<List<GitleaksDTO.Result>>() {
                });

        GitleaksDTO dto = GitleaksDTO.builder().results(list).build();

        j.setLog(logConverter.convertToDatabaseColumn(dto));
        j.setCompliant(list.isEmpty());
    }

    private void consumeGokart(String s, Job j) {
        List<GoKartDTO.Result> list = logConverter.convertToEntityAttribute(s,
                new TypeReference<List<GoKartDTO.Result>>() {
                });

        GoKartDTO dto = GoKartDTO.builder().results(list).build();

        j.setLog(logConverter.convertToDatabaseColumn(dto));
        j.setCompliant(list.isEmpty());
    }

    private void consumeProgpilot(String s, Job j) {
        List<ProgPilotDTO.Result> list = logConverter.convertToEntityAttribute(s,
                new TypeReference<List<ProgPilotDTO.Result>>() {
                });

        ProgPilotDTO dto = ProgPilotDTO.builder().results(list).build();

        j.setLog(logConverter.convertToDatabaseColumn(dto));
        j.setCompliant(list.isEmpty());
    }

    private void consumeFlawfinder(String s, Job j) {
        Sarif210Rtm5 dto = logConverter.convertToEntityAttribute(s, Sarif210Rtm5.class);

        j.setLog(s);
        j.setCompliant(dto.getRuns().isEmpty());
    }

}
