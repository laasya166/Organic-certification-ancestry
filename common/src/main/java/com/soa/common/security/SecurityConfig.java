package com.soa.common.security;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Shared JWT resource-server config (HS256, validates signature AND expiry).
 * app.security.enabled=false  -> everything open (use while building locally)
 * app.security.enabled=true   -> JWT required, roles come from the "roles" claim
 * Use @PreAuthorize("hasRole('processor')") on controllers.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.enabled:false}")
    private boolean enabled;

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable());
        if (!enabled) {
            http.authorizeHttpRequests(a -> a.anyRequest().permitAll());
            return http.build();
        }
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health/**", "/auth/login").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter())));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private JwtAuthenticationConverter converter() {
        JwtGrantedAuthoritiesConverter g = new JwtGrantedAuthoritiesConverter();
        g.setAuthoritiesClaimName("roles");
        g.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(g);
        return c;
    }
}
