/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication Filter based on {@link OncePerRequestFilter}.
 */
@Slf4j
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String[] AUTH_WHITELIST = {
            "\\/v3\\/api-docs(\\/.*)?",
            "\\/swagger-ui\\/.*",
            "\\/api\\/v3/api-docs(\\/.*)?",
            "\\/api\\/swagger-ui\\/.*",
            "\\/actuator\\/.*",
            "\\/users\\/me",
            "\\/version",
            "\\/administrator\\/.*",
            "\\/business-hours"
    };

    /**
     * Auth Service.
     */
    @Autowired
    private AuthService authService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().matches(String.join("|", AUTH_WHITELIST));
    }

    /**
     * Check that the requested organization is authorized.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        try {
            UserBO userInfo = authService.verifyUserAuthentication();

            final String[] urlSplit = request.getRequestURI().split("/");
            final Pair<String, String> subOrg = authService.getSubscriberAndOrganization(urlSplit);

            JwtAuthenticationToken newAuth = authService.getJwtToken(userInfo, subOrg.getFirst(), Long.parseLong(subOrg.getSecond()));
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            authService.checkUserRightForDigitalService(urlSplit);
        } catch (AuthorizationException e) {
            log.error(e.getMessage());
            response.sendError(e.getStatusCode(), e.getMessage());
            return;
        } catch (Exception e) {
            log.error("Unexpected exception", e);
            response.sendError(500, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }


}
