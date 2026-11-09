
package com.farwa.notesapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // Only ADMIN can manage users
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // ADMIN, USER and VIEWER can read
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notes/**",
                                "/api/categories/**",
                                "/api/tags/**",
                                "/api/reminders/**",
                                "/api/attachments/**"
                        ).hasAnyRole("ADMIN", "USER", "VIEWER")

                        // Only ADMIN and USER can create or modify
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/notes/**",
                                "/api/categories/**",
                                "/api/tags/**",
                                "/api/reminders/**",
                                "/api/attachments/**"
                        ).hasAnyRole("ADMIN", "USER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/notes/**",
                                "/api/categories/**",
                                "/api/tags/**",
                                "/api/reminders/**",
                                "/api/attachments/**"
                        ).hasAnyRole("ADMIN", "USER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/notes/**",
                                "/api/categories/**",
                                "/api/tags/**",
                                "/api/reminders/**",
                                "/api/attachments/**"
                        ).hasAnyRole("ADMIN", "USER")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/notes/**",
                                "/api/categories/**",
                                "/api/tags/**",
                                "/api/reminders/**",
                                "/api/attachments/**"
                        ).hasAnyRole("ADMIN", "USER")

                        // Deny all other endpoints
                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken>
    keycloakJwtConverter() {

        return jwt -> {

            Collection<GrantedAuthority> authorities =
                    new ArrayList<>();

            Object realmAccessObject =
                    jwt.getClaim("realm_access");

            if (realmAccessObject instanceof Map<?, ?> realmAccess) {

                Object rolesObject = realmAccess.get("roles");

                if (rolesObject instanceof List<?> roles) {

                    for (Object role : roles) {

                        if (role instanceof String roleName) {

                            if (roleName.equals("ADMIN")
                                    || roleName.equals("USER")
                                    || roleName.equals("VIEWER")) {

                                authorities.add(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + roleName
                                        )
                                );
                            }
                        }
                    }
                }
            }

            return new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    jwt.getSubject()
            );
        };
    }
}
