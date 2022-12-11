package com.dsec.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsec.backend.entity.Repo;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {

    boolean existsByFullName(String fullName);

    Repo findByFullName(String fullName);

}
