/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Business Object.
 */
@Data
@SuperBuilder
public class UserBO {

    long id;

    /**
     * The email.
     */
    private String email;

    /**
     * The firstName of the user.
     */
    private String firstName;

    /**
     * The lastName of the user.
     */
    private String lastName;

    /**
     * The subject of the user.
     */
    private String sub;

    /**
     * The domain of the user.
     */
    private String domain;

    /**
     * User's subscriber.
     */
    private List<SubscriberBO> subscribers;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

}
