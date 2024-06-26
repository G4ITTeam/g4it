/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Security Configuration
 */
@Configuration
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {

    /**
     * URI WhiteList.
     */
    private static final AntPathRequestMatcher[] AUTH_WHITELIST = {
            antMatcher("/v3/api-docs"),
            antMatcher("/v3/api-docs/swagger-config"),
            antMatcher("/swagger-ui/**"),
            antMatcher("/api/v3/api-docs"),
            antMatcher("/api/v3/api-docs/swagger-config"),
            antMatcher("/api/swagger-ui/**"),
            antMatcher("/actuator/**")
    };

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(auth -> auth.requestMatchers(AUTH_WHITELIST).permitAll())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .cors(Customizer.withDefaults())
                .build();
    }

}