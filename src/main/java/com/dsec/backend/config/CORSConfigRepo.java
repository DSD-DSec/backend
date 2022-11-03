package com.dsec.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.dsec.backend.model.UserModel;
import com.dsec.backend.model.UserRole;

@Configuration
public class CORSConfigRepo implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        cors.addMapping("/**").allowedOrigins("*").allowedMethods("*");
        config.exposeIdsFor(UserModel.class, UserRole.class);
    }
}
