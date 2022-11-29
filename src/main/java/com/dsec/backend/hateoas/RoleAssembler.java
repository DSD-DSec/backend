package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.entity.UserRole;

@Component
public class RoleAssembler extends RepresentationModelAssemblerSupport<UserRole, UserRole> {

    RoleAssembler() {
        super(RoleController.class, UserRole.class);
    }

    @Override
    public UserRole toModel(UserRole entity) {

        entity.add(
                linkTo(
                        methodOn(RoleController.class).getRole(entity.getId())).withSelfRel(),
                linkTo(
                        methodOn(RoleController.class)
                                .getRoles()).withSelfRel());

        return entity;
    }

}
