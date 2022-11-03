package com.dsec.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class ApiConfig {

    // @Bean
    // public RepresentationModelProcessor<EntityModel<UserKamin>> personProcessor() {

    //     return new RepresentationModelProcessor<EntityModel<UserKamin>>() {

    //         @Override
    //         public EntityModel<UserKamin> process(EntityModel<UserKamin> model) {

    //             model.add(linkTo(methodOn(UserController.class).stats(model.getContent().getId())).withRel("stats")
    //                     .withType("get"));
    //             model.add(linkTo(methodOn(UserController.class).userRoom()).withRel("userHasAccess").withType("get"));
    //             return model;
    //         }
    //     };
    // }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

}
