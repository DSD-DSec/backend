package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.controller.UserController;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.model.user.UserDTO;
import com.dsec.backend.model.user.UserRoleDTO;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<UserEntity, UserDTO> {
    public UserAssembler() {
        super(UserController.class, UserDTO.class);
    }

    @Override
    public UserDTO toModel(UserEntity entity) {
        UserDTO userDTO = UserDTO.builder().id(entity.getId())
                .email(entity.getEmail()).firstName(entity.getFirstName())
                .lastName(entity.getLastName()).userRole(toRoleModel(entity.getUserRole())).build();


        Link link = linkTo(methodOn(UserController.class).getById(entity.getId())).withSelfRel();
        var methodInvocation = methodOn(UserController.class);

        userDTO.add(link
                .andAffordance(
                        afford(methodInvocation.getMe(null)))
                .withRel("me")
                .andAffordance(
                        afford(methodInvocation.logout(null, null)))
                .withRel("logout")
                .andAffordance(
                        afford(methodInvocation.deleteUser(entity.getId(), null)))
                .withRel("delete")
                .andAffordance(
                        afford(methodInvocation.updateUser(entity.getId(), null, null)))
                .withRel("update"),

                linkTo(
                        methodInvocation.getUsers(null, null, null, null, null, null))
                                .withRel("users"));

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
