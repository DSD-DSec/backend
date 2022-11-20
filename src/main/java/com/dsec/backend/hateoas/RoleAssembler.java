package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.model.user.UserRoleDTO;

@Component
public class RoleAssembler extends RepresentationModelAssemblerSupport<UserRole, UserRoleDTO> {

    RoleAssembler() {
        super(RoleController.class, UserRoleDTO.class);
    }

    @Override
    public UserRoleDTO toModel(UserRole entity) {
        UserRoleDTO dto = createModelWithId(entity.getId(), entity);

        dto.add(linkTo(
                methodOn(RoleController.class)
                        .getRoles()).withSelfRel());

        dto.setId(entity.getId());
        dto.setRoleName(entity.getRoleName());

        return dto;
    }

}
