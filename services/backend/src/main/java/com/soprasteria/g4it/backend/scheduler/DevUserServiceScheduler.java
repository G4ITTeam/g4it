/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserSubscriber;
import com.soprasteria.g4it.backend.apiuser.repository.RoleRepository;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserSubscriberRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev & !test")
@Slf4j
public class DevUserServiceScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private UserSubscriberRepository userSubscriberRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Execute the data deletion with a cron scheduler
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void createAdminDevUser() {
        String email = "admin@dev.com";
        String subscriberName = "SOPRA-STERIA-GROUP";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        Subscriber subscriber = subscriberRepository.findByName(subscriberName).orElse(null);
        if (subscriber == null) return;

        if (user.getUserSubscribers() == null || !user.getUserSubscribers().isEmpty()) return;

        Role roleAdminSubscriber = roleRepository.findByName(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR);

        userSubscriberRepository.save(UserSubscriber.builder()
                .user(user)
                .subscriber(subscriber)
                .defaultFlag(true)
                .roles(List.of(roleAdminSubscriber))
                .build());

        log.info("User {} has been granted subscriber admin role on {}", email, subscriber.getName());
    }
}
