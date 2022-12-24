package com.dsec.backend.security;

import java.util.Optional;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import com.dsec.backend.config.ConfigProperties;
import com.dsec.backend.security.oauth.CustomOAuth2UserService;
import com.dsec.backend.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.dsec.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.dsec.backend.util.cookie.CookieUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
@Profile("prod")
public class SecurityFilterChainConfigurerProd {

    private final ConfigProperties configProperties;
    private final UserDetailsService myUserDetailsService;
    private final CookieUtil cookieUtil;
    private final CustomOAuth2UserService userService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // @formatter:off

        return http.cors().and().csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers()
                    .frameOptions().disable()
                    .and()
                .formLogin()
                    .disable()
                .httpBasic()
                    .disable()
                .oauth2ResourceServer(
                        (customizer) -> customizer.jwt().and()
                        .bearerTokenResolver(getTokenResolver())
                        .authenticationEntryPoint(getAuthenticationEntryPoint()))

                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))

                .authorizeRequests((authorizeRequests) -> authorizeRequests
                        .antMatchers("/auth/**", "/v3/api-docs*/**", "/", "/github/webhook", "/login/oauth2/code/github*/**")
                        .permitAll()
                        .antMatchers("/swagger-ui*/**").denyAll()
                        .antMatchers("/**").access("hasAnyAuthority('SCOPE_USER','SCOPE_ADMIN')")
                        .anyRequest().authenticated())

                .oauth2Login()
                    .authorizationEndpoint()
                        .authorizationRequestRepository(authorizationRequestRepository)
                        .and()
                    .userInfoEndpoint()
                        .userService(userService)
                        .and()
                    .successHandler(successHandler)
                .and()
                .userDetailsService(myUserDetailsService)

                .build();
        
        // @formatter:on

    }

    private AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return new MyAuthenticationEntryPoint(cookieUtil);
    }

    private BearerTokenResolver getTokenResolver() {
        return (request) -> {
            Optional<Cookie> cookie = cookieUtil.getCookie(request, configProperties.getJwt().getCookieName());

            return cookie.map(Cookie::getValue).orElse(null);

        };
    }

}
