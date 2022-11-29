package com.dsec.backend.service;

import org.springframework.security.oauth2.jwt.Jwt;

import com.dsec.backend.entity.Repo;

import reactor.core.publisher.Mono;

public interface GithubClientService {

    Mono<String> getUser(Jwt jwt);

    Mono<String> getRepos(Jwt jwt);

    Mono<Repo> getRepo(String fullRepoName, Jwt jwt);

    Mono<String> createWebHook(String fullRepoName, Jwt jwt);

    void triggerHook(String hookUrl, Jwt jwt);
}
