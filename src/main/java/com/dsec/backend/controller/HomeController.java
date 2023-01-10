package com.dsec.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dsec.backend.hateoas.EmptyAssembler;
import com.dsec.backend.model.EmptyDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomeController {
    private final EmptyAssembler emptyAssembler;

    @GetMapping("/")
    public ResponseEntity<EmptyDTO> getHome() {
        return ResponseEntity.ok(emptyAssembler.toModel(new EmptyDTO()));
    }

}
