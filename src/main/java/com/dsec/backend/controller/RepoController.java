package com.dsec.backend.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.hateoas.RepoAssembler;
import com.dsec.backend.model.EmptyDTO;
import com.dsec.backend.model.job.JobDTO;
import com.dsec.backend.model.repo.CreateRepoDTO;
import com.dsec.backend.model.tools.RepoToolUpdateDTO;
import com.dsec.backend.service.job.JobService;
import com.dsec.backend.service.repo.RepoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/repo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepoController {
    private final RepoService repoService;
    private final JobService jobService;
    private final RepoAssembler repoAssembler;

    @PostMapping("/trigger/{id}")
    public ResponseEntity<EmptyDTO> triggerHook(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) {
        repoService.triggerHook(id, jwt);
        return ResponseEntity.ok(new EmptyDTO());
    }

    @PostMapping("/{owner}/{repo}")
    public ResponseEntity<Repo> createRepo(@PathVariable("owner") String owner,
            @PathVariable("repo") String repoName, @Valid @RequestBody CreateRepoDTO createRepoDTO,
            @AuthenticationPrincipal Jwt jwt) {

        Repo repo = repoService.createRepo(owner + "/" + repoName, createRepoDTO, jwt);

        return ResponseEntity.ok(repoAssembler.toModel(repo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repo> getRepoById(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt) {
        Repo repo = repoAssembler.toModel(repoService.getById(id, jwt));

        return ResponseEntity.ok(repoAssembler.toModel(repo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Repo> deleteRepo(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt) {
        Repo repo = repoService.fetch(id);

        repoService.deleteRepo(repo, jwt);

        return ResponseEntity.ok(repoAssembler.toModel(repo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Repo> updateRepo(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CreateRepoDTO createRepoDTO) {
        Repo repo = repoService.fetch(id);

        repoService.updateRepo(id, repo, createRepoDTO, jwt);

        return ResponseEntity.ok(repoAssembler.toModel(repo));
    }

    // Method used only for RepoAssembler and hateos
    public ResponseEntity<Repo> fetchRepo(Long id) {
        Repo repo = repoAssembler.toModel(repoService.fetch(id));

        return ResponseEntity.ok(repo);
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<List<JobDTO<?>>> getJobs(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jobService.getJobsByRepoID(id, jwt));
    }

    @GetMapping("/{id}/tools")
    public ResponseEntity<List<ToolEntity>> getTools(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(repoService.getToolsByRepo(id, jwt));
    }

    @PutMapping("/{id}/tools")
    public ResponseEntity<List<ToolEntity>> updateTools(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt, @RequestBody @Valid RepoToolUpdateDTO repoToolUpdateDTO) {

        return ResponseEntity.ok(repoService.updateRepoTools(id, repoToolUpdateDTO, jwt));
    }

}
