package com.dsec.backend.service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.model.repo.CreateRepoDTO;

public interface RepoService {

    Page<Repo> getRepos(Pageable pageable);

    Repo createRepo(String fullName, CreateRepoDTO createRepoDTO, Jwt jwt);

    Repo deleteRepo(Repo repo, Jwt jwt);

    Repo updateRepo(long id, Repo repo, CreateRepoDTO createRepoDTO, Jwt jwt);

    Repo fetch(long id);

    Repo getById(long id, Jwt jwt);

    void triggerHook(long id, Jwt jwt);

    Repo fetchByGithubId(long githubId);

}