/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend;


import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;

import java.util.List;

public class TestUtils {

    public static String SUBSCRIBER = "SUBSCRIBER";
    public static String ORGANIZATION = "ORGANIZATION";
    public static String USERNAME = "user.test.unitaire";

    public static UserBO createUserBO(final List<String> userRoles) {
        return UserBO.builder()
                .username(USERNAME)
                .subscribers(List.of(SubscriberBO.builder()
                        .name(SUBSCRIBER)
                        .organizations(List.of(OrganizationBO.builder()
                                .name(ORGANIZATION)
                                .roles(userRoles)
                                .build()))
                        .build()))
                .build();
    }

    public static Organization createOrganization() {
        return Organization.builder()
                .name(ORGANIZATION)
                .subscriber(Subscriber.builder()
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }
}
