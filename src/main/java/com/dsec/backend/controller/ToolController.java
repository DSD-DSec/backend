package com.dsec.backend.controller;


import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.service.tool.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/tool")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ToolController {


    private final ToolService toolService;


    @GetMapping("")
    public ResponseEntity<List<ToolEntity>> getTools() {
        return ResponseEntity.ok(toolService.getTools());
    }
}
