package com.dsec.backend.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.dsec.backend.controller.AuthController;
import com.dsec.backend.controller.HomeController;
import com.dsec.backend.controller.RoleController;
import com.dsec.backend.controller.UserController;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.model.EmptyDTO;
import com.dsec.backend.model.user.LoginDTO;
import com.dsec.backend.model.user.UserRegisterDTO;

@Component
public class EmptyAssembler extends RepresentationModelAssemblerSupport<EmptyDTO, EmptyDTO> {

    public EmptyAssembler() {
        super(HomeController.class, EmptyDTO.class);
    }

    @Override
    public EmptyDTO toModel(EmptyDTO entity) {
        entity.add(linkTo(methodOn(HomeController.class).getHome()).withSelfRel());

        entity.add(Affordances
                .of(linkTo(methodOn(AuthController.class).register(null)).withRel("register")
                        .withType(HttpMethod.POST.name()))
                .afford(HttpMethod.POST).withInput(UserRegisterDTO.class)
                .withOutput(UserEntity.class).toLink());

        entity.add(Affordances
                .of(linkTo(methodOn(AuthController.class).login(null, null)).withRel("login")
                        .withType(HttpMethod.POST.name()))
                .afford(HttpMethod.POST).withInput(LoginDTO.class)
                .withOutput(UserEntity.class).toLink());

        entity.add(Affordances
                .of(linkTo(
                        methodOn(UserController.class).getUsers(null, null, null, null, null, null))
                                .withRel("users").withType(HttpMethod.GET.name()))
                .afford(HttpMethod.GET).toLink());

        entity.add(Affordances
                .of(linkTo(
                        methodOn(RoleController.class).getRoles())
                                .withRel("roles").withType(HttpMethod.GET.name()))
                .afford(HttpMethod.GET).toLink());

        return entity;

    }



}
