package com.dsec.backend.service;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.model.github.UserDTO;

import reactor.core.publisher.Mono;

public interface GithubClientService {

    Mono<UserDTO> getUser(Jwt jwt);

    Mono<List<RepoDTO>> getRepos(Jwt jwt);

    Mono<RepoDTO> getRepo(String fullRepoName, Jwt jwt);

    Mono<String> createWebHook(String fullRepoName, Jwt jwt);

    void triggerHook(String hookUrl, Jwt jwt);
}
