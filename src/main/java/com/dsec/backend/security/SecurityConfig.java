package com.dsec.backend.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${jwt.public.key}")
	private RSAPublicKey key;

	@Value("${jwt.private.key}")
	private RSAPrivateKey priv;

	@Value("${jwt.cookie.name}")
	private String cookieName;

	@Value("${cors.allow.origins}")
	private String corsOrigins;

	@Autowired
	private UserDetailsService myUserDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		return http.cors().and().csrf(csrf -> csrf.disable())
				.sessionManagement(
						(session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.headers().frameOptions().disable().and()

				.oauth2ResourceServer((customizer) -> customizer.jwt().and()
						.bearerTokenResolver(getTokenResolver()))

				.exceptionHandling((exceptions) -> exceptions
						.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
						.accessDeniedHandler(new BearerTokenAccessDeniedHandler()))

				.authorizeRequests((authorizeRequests) -> authorizeRequests
						.antMatchers("/auth/**", "/h2-console/**", "/v3/api-docs*/**",
								"/swagger-ui*/**")
						.permitAll()
						.antMatchers("/**").access("hasAnyAuthority('SCOPE_USER','SCOPE_ADMIN')")
						.anyRequest().authenticated())

				.formLogin().disable()

				.userDetailsService(myUserDetailsService)

				.build();

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

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		WebSecurityCustomizer websc = (web) -> {
		};
		return websc;
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		// config.setAllowCredentials(true);
		config.setAllowedOrigins(List.of(corsOrigins.split(",")));
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.key).build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
