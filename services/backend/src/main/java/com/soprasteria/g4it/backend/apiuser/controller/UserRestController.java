/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorOrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.server.gen.api.UserApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * User Rest Service.
 */
@Service
public class UserRestController implements UserApiDelegate {

    /**
     * Auth Service.
     */
    @Autowired
    private AuthService authService;

    /**
     * UserRest Mapper.
     */
    @Autowired
    private UserRestMapper userRestMapper;

    @Autowired
    AdministratorOrganizationService administratorOrganizationService;

    @Autowired
    private OrganizationRestMapper organizationRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserRest> getUser() {
        return ResponseEntity.ok(userRestMapper.toDto(authService.getUser()));
    }

    @Override
    public ResponseEntity<OrganizationRest> createNewOrganization(OrganizationUpsertRest organizationUpsertRest) {
        return new ResponseEntity<>(organizationRestMapper.toDto(administratorOrganizationService.createOrganization(organizationUpsertRest, authService.getAdminUser())),
                HttpStatus.OK);
    }
}
