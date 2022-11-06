package com.dsec.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class ApiConfig {

    // @Bean
    // public RepresentationModelProcessor<EntityModel<UserInfoDTO>> personProcessor() {

    // return new RepresentationModelProcessor<EntityModel<UserInfoDTO>>() {

    // @Override
    // public EntityModel<UserInfoDTO> process(EntityModel<UserInfoDTO> model) {



    // return null;
    // }

    // };
    // }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

}
