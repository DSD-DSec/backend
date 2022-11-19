package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.controller.UserController;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.model.UserDTO;
import com.dsec.backend.model.UserRoleDTO;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<UserEntity, UserDTO> {
    public UserAssembler() {
        super(UserController.class, UserDTO.class);
    }

    @Override
    public UserDTO toModel(UserEntity entity) {
        UserDTO userDTO = createModelWithId(entity.getId(), entity);

        userDTO.add(
                linkTo(
                        methodOn(UserController.class)
                                .getUsers(null, "firstName", "lastName", "email", "generalSearch",
                                        null)).withRel("users"));

        // TODO add links to user PATCH and DELETE methods when you create them

        userDTO.setId(entity.getId());
        userDTO.setFirstName(entity.getFirstName());
        userDTO.setLastName(entity.getLastName());
        userDTO.setUserRole(toRoleModel(entity.getUserRole()));
        userDTO.setEmail(entity.getEmail());

        return userDTO;
    }

    private UserRoleDTO toRoleModel(UserRole userRole) {
        return addLinks(
                UserRoleDTO.builder().id(userRole.getId()).roleName(userRole.getRoleName()).build(),
                userRole);
    }

    private UserRoleDTO addLinks(UserRoleDTO dto, UserRole userRole) {
        Link link = linkTo(
                methodOn(RoleController.class)
                        .getRole(userRole.getId())).withSelfRel();
        return dto.add(link);
    }

}
