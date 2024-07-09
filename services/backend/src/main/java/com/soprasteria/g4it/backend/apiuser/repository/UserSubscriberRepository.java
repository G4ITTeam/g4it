/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriberRepository extends JpaRepository<UserSubscriber, Long> {

    /**
     * Find userSubscribers by subscriber
     *
     * @param subscriber
     * @return the list of userSubscribers
     */
    List<UserSubscriber> findBySubscriber(Subscriber subscriber);
}
