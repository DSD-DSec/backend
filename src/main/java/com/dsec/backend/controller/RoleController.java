package com.dsec.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.hateoas.RoleAssembler;
import com.dsec.backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/userRoles")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleController {
    private final RoleService roleService;
    private final RoleAssembler roleAssembler;

    @GetMapping("")
    public ResponseEntity<CollectionModel<UserRole>> getRoles() {
        log.debug("Get roles request");

        return ResponseEntity.ok(roleAssembler.toCollectionModel(roleService.getUserRoles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRole> getRole(@PathVariable int id) {
        log.debug("Get roles request");

        return ResponseEntity.ok(roleAssembler.toModel(roleService.getRole(id)));
    }

}
