package com.dsec.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsec.backend.entity.Job;
import com.dsec.backend.service.JobService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/job")
public class JobController {

    private final JobService jobService;

    @GetMapping("")
    public ResponseEntity<List<Job>> getJobs() {
        return ResponseEntity.ok(jobService.findAll());
    }

    @GetMapping("/{job}")
    public ResponseEntity<Job> getJobLog(@PathVariable("job") long jobID, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jobService.fetch(jobID, jwt));
    }
}
