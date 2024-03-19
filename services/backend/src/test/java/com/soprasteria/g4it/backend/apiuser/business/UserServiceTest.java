/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void givenBearer_thenReturnUser() {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(Jwt.withTokenValue("XXXXX")
                .claim("unique_name", USERNAME)
                .header("Authorization", "Bearer XXXXX")
                .build());
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(User.builder().username(USERNAME)
                .userSubscribers(List.of(UserSubscriber.builder().defaultFlag(true).subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build()))
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name("ROLE_INVENTORY_READ").build())).organization(Organization.builder().name(ORGANIZATION)
                                .subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build()).build())).build()));

        final UserBO user = userService.getUser();

        Assertions.assertThat(user).isNotNull();

        verify(userRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void givenBearerWithUnknownDomain_thenThrow() {
        assertThatThrownBy(() -> userService.getUser())
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("The token is not a JWT token");
    }
}
