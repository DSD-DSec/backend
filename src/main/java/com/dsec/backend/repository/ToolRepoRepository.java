package com.dsec.backend.repository;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.ToolRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Set;

public interface ToolRepoRepository extends JpaRepository<ToolRepo, Long> {

    Set<ToolRepo> getToolRepoByRepo(Repo repo);

    @Transactional
    void deleteToolRepoByRepo(Repo repo);
}
