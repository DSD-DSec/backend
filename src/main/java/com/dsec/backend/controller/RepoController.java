package com.dsec.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsec.backend.model.EmptyDTO;
import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.service.RepoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/repo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepoController {
    private final RepoService repoService;

    private final JobService jobService;

    private final RepoAssembler repoAssembler;

    /*
    @PostMapping("/{owner}/{repo}")
    public ResponseEntity<RepoDTO> createRepo(@PathVariable("owner") String owner,
            @PathVariable("repo") String repoName,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(repoService.createRepo(owner + "/" + repoName, jwt));
    }
    */

    @PostMapping("/trigger/{id}")
    public ResponseEntity<EmptyDTO> triggerHook(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) {
        repoService.triggerHook(id, jwt);
        return ResponseEntity.ok(new EmptyDTO());
    }

    @PostMapping("/create")
    public ResponseEntity<Repo> createRepo(@Valid @RequestBody RepoDTO repoDTO,
                                           @AuthenticationPrincipal Jwt jwt) {

        Repo repo = repoService.createRepo(repoDTO, jwt);

        return ResponseEntity.ok(repo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repo> getRepoById(@PathVariable("id") long id) {
        Repo repo = repoAssembler.toModel(repoService.fetch(id));

        return ResponseEntity.ok(repo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Repo> deleteRepo(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt) {
        Repo repo = repoService.fetch(id);

        repoService.deleteRepo(repo, jwt);

        return ResponseEntity.ok(repoAssembler.toModel(repo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Repo> updateRepo(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt,
                                           @RequestBody @Valid RepoUpdateDTO repoUpdateDTO) {
        Repo repo = repoService.fetch(id);

        repoService.updateRepo(id, repo, repoUpdateDTO, jwt);

        return ResponseEntity.ok(repoAssembler.toModel(repo));
    }



    @GetMapping("/{repoId}/jobs")
    public ResponseEntity<List<Job>> getJobs(@PathVariable("repoId") long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jobService.getJobsByRepoID(id, jwt));
    }

}
