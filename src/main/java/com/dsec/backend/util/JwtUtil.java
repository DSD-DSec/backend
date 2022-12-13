package com.dsec.backend.util;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtClaimsSet.Builder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.dsec.backend.security.UserPrincipal;

@Component
public class JwtUtil {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String getToken(UserPrincipal userPrincipal, Long jwtExpiry) {
        Instant now = Instant.now();

        String scope = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Builder builder = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpiry))
                .subject(userPrincipal.getUsername())
                .claim("scope", scope);

        for (var e : userPrincipal.getClaims().entrySet()) {
            builder.claim(e.getKey(), e.getValue());
        }
        JwtClaimsSet claims = builder.build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getOAuthToken(UserPrincipal userPrincipal, Long jwtExpiry) {
        Instant now = Instant.now();

        Builder builder = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpiry))
                .subject(userPrincipal.getUserEntity().getId().toString());

        JwtClaimsSet claims = builder.build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Long getUserIdFromOAuthToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return Long.valueOf(jwt.getSubject());
        } catch (JwtException e) {
            return null;
        }
    }

}
