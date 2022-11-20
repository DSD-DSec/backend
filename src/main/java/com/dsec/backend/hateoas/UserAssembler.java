package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.controller.UserController;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.model.user.UserUpdateDTO;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<UserEntity, UserEntity> {
        public UserAssembler() {
                super(UserController.class, UserEntity.class);
        }

        @Override
        public UserEntity toModel(UserEntity entity) {


                Link link = Affordances
                                .of(linkTo(methodOn(UserController.class).getById(entity.getId()))
                                                .withSelfRel().withType(HttpMethod.GET.name()))
                                .afford(HttpMethod.DELETE)
                                .withOutput(UserEntity.class)
                                .withName("delete")
                                .andAfford(HttpMethod.PUT).withInput(UserUpdateDTO.class)
                                .withOutput(UserEntity.class).withName("update").toLink();


                entity.add(link);

                var methodInvocation = methodOn(UserController.class);

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
