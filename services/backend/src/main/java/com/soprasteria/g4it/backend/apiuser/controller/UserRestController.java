/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.controller;

import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.server.gen.api.UserApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * User Rest Service.
 */
@Service
public class UserRestController implements UserApiDelegate {

    /**
     * User Service.
     */
    @Autowired
    private UserService userService;

    /**
     * UserRest Mapper.
     */
    @Autowired
    private UserRestMapper userRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserRest> getUser() {
        return ResponseEntity.ok(userRestMapper.toDto(userService.getUser()));
    }
}
