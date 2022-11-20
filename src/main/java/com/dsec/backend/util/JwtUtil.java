package com.dsec.backend.util;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtClaimsSet.Builder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import com.dsec.backend.security.UserPrincipal;

@Component
public class JwtUtil {

    private JwtEncoder jwtEncoder;

    @Autowired
    public JwtUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
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

}
