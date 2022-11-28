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

    @PostMapping("/{owner}/{repo}")
    public ResponseEntity<RepoDTO> createRepo(@PathVariable("owner") String owner,
            @PathVariable("repo") String repoName,
            @AuthenticationPrincipal Jwt jwt) throws JsonMappingException, JsonProcessingException {

        return ResponseEntity.ok(repoService.createRepo(owner + "/" + repoName, jwt));
    }

    @PostMapping("/trigger/{id}")
    public ResponseEntity<EmptyDTO> triggerHook(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) throws JsonMappingException, JsonProcessingException {
        repoService.triggerHook(id, jwt);
        return ResponseEntity.ok(new EmptyDTO());
    }
}
