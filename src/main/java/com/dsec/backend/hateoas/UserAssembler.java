package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.controller.UserController;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRole;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<UserEntity, UserEntity> {
        public UserAssembler() {
                super(UserController.class, UserEntity.class);
        }

        @Override
        public UserEntity toModel(UserEntity entity) {

                Link link = linkTo(methodOn(UserController.class).getById(entity.getId()))
                                .withSelfRel().withType(HttpMethod.GET.name());

                var methodInvocation = methodOn(UserController.class);

                entity.add(link);
                entity.add(link.andAffordance(
                                afford(methodOn(UserController.class).deleteUser(entity.getId(),
                                                null)))
                                .withRel("delete").withType(HttpMethod.DELETE.name()));
                entity.add(link.andAffordance(
                                afford(methodOn(UserController.class).updateUser(entity.getId(),
                                                null, null)))
                                .withRel("update").withType(HttpMethod.PUT.name()));


                entity.add(linkTo(methodInvocation.getUsers(null, null, null, null, null, null))
                                .withRel("users").withType(HttpMethod.GET.name()));
                entity.add(linkTo(methodInvocation.getMe(null)).withRel("me")
                                .withType(HttpMethod.GET.name()));
                entity.add(linkTo(methodInvocation.logout(null, null)).withRel("logout")
                                .withType(HttpMethod.POST.name()));

                entity.setUserRole(toRoleModel(entity.getUserRole()));

                return entity;
        }

        private UserRole toRoleModel(UserRole userRole) {
                return userRole.add(linkTo(
                                methodOn(RoleController.class)
                                                .getRole(userRole.getId())).withSelfRel());
        }

}
