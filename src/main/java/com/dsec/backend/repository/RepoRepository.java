package com.dsec.backend.repository;

import java.util.Optional;

import com.dsec.backend.entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dsec.backend.entity.Repo;

public interface RepoRepository extends JpaRepository<Repo, Long> {

    boolean existsByFullName(String fullName);

    Repo findByFullName(String fullName);

    Optional<Repo> findByGithubId(Long githubId);



}
