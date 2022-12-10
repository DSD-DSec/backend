package com.dsec.backend.controller;

import com.dsec.backend.entity.Job;
import com.dsec.backend.model.EmptyDTO;
import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.service.JobService;
import com.dsec.backend.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/repo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepoController {
    private final RepoService repoService;

    private final JobService jobService;

    @PostMapping("/{owner}/{repo}")
    public ResponseEntity<RepoDTO> createRepo(@PathVariable("owner") String owner,
            @PathVariable("repo") String repoName,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(repoService.createRepo(owner + "/" + repoName, jwt));
    }

    @PostMapping("/trigger/{id}")
    public ResponseEntity<EmptyDTO> triggerHook(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) {
        repoService.triggerHook(id, jwt);
        return ResponseEntity.ok(new EmptyDTO());
    }

    @GetMapping("/{repoId}/jobs")
    public ResponseEntity<List<Job>> getJobs(@PathVariable("repoId") long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jobService.getJobsByRepoID(id, jwt));
    }

}
