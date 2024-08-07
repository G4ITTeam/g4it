/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Test
    void hasAdminRightsOnAnySubscriber_hasAdminRole() {
        assertTrue(roleService.hasAdminRightsOnAnySubscriber(TestUtils.createUserBOAdminSub()));
    }

    @Test
    void hasAdminRightsOnAnySubscriber_NoAdminRole() {
        assertFalse(roleService.hasAdminRightsOnAnySubscriber(TestUtils.createUserBONoRole()));
    }

    @Test
    void hasAdminRightsOnSubscriber_hasAdminRole() {
        assertTrue(roleService.hasAdminRightsOnSubscriber(TestUtils.createUserBOAdminSub(), 2L));
    }

    @Test
    void hasAdminRightsOnSubscriber_NoAdminRole() {
        // the user is subscriber admin on subscriber 2, not on 1
        assertFalse(roleService.hasAdminRightsOnSubscriber(TestUtils.createUserBOAdminSub(), 1L));
    }
}
