package com.dsec.backend.security;

import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import com.dsec.backend.util.cookie.CookieUtil;

@Configuration
@Profile("prod")
public class SecurityFilterChainConfigurerProd {
    private String cookieName;

    private UserDetailsService myUserDetailsService;
    private CookieUtil cookieUtil;

    @Autowired
    public SecurityFilterChainConfigurerProd(@Value("${jwt.cookie.name}") String cookieName,
            UserDetailsService myUserDetailsService, CookieUtil cookieUtil) {
        this.cookieName = cookieName;
        this.myUserDetailsService = myUserDetailsService;
        this.cookieUtil = cookieUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http.cors().and().csrf(csrf -> csrf.disable())
                .sessionManagement(
                        (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers().frameOptions().disable().and()

                .oauth2ResourceServer((customizer) -> customizer.jwt().and()
                        .bearerTokenResolver(getTokenResolver())
                        .authenticationEntryPoint(getAuthenticationEntryPoint()))

                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))

                .authorizeRequests((authorizeRequests) -> authorizeRequests
                        .antMatchers("/auth/**", "/v3/api-docs*/**", "/")
                        .permitAll()
                        .antMatchers("/swagger-ui*/**").denyAll()
                        .antMatchers("/**").access("hasAnyAuthority('SCOPE_USER','SCOPE_ADMIN')")
                        .anyRequest().authenticated())

                .formLogin().disable()

                .userDetailsService(myUserDetailsService)

                .build();

    }

    private AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return new MyAuthenticationEntryPoint(cookieUtil);
    }

    private BearerTokenResolver getTokenResolver() {
        return (request) -> {
            Cookie[] cookies = request.getCookies();
            Optional<Cookie> cookie = Optional.empty();
            if (cookies != null) {
                cookie = Arrays.stream(cookies).filter(c -> c.getName().equals(cookieName))
                        .findAny();
            }

            if (!cookie.isEmpty()) {
                return cookie.get().getValue();
            }

            return null;
        };
    }

}
