/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.modeldb;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * G4IT User Subscriber mapping.
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "g4it_user_subscriber")
public class UserSubscriber implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The subscriber.
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "subscriber_id", referencedColumnName = "id")
    private Subscriber subscriber;

    /**
     * The user.
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * Flag 'Default'.
     */
    private Boolean defaultFlag;
}
