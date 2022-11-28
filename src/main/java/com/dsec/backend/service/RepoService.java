package com.dsec.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.model.github.RepoDTO;

public interface RepoService {

    Page<Repo> getRepos(Pageable pageable);

    RepoDTO createRepo(String fullRepoName, Jwt jwt);

    Repo fetch(long id);

    void triggerHook(long id, Jwt jwt);

}
