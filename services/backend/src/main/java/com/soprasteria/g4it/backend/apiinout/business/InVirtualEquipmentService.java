/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class InVirtualEquipmentService {

    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;
    private DigitalServiceRepository digitalServiceRepository;
    private InventoryRepository inventoryRepository;

    /**
     * Get the virtual equipments list linked to a digital service.
     *
     * @param digitalServiceUid the digital service UID.
     * @return the virtual equipment list.
     */
    public List<InVirtualEquipmentRest> getByDigitalService(final String digitalServiceUid) {
        final List<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceUid(digitalServiceUid);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipment);
    }

    /**
     * Retrieving a virtual equipment for a digital service and a virtual equipment id.
     *
     * @param digitalServiceUid the digital service UID.
     * @param id                the virtual equipment id
     * @return InVirtualEquipmentBO
     */
    public InVirtualEquipmentRest getByDigitalServiceAndId(final String digitalServiceUid, Long id) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no virtual equipment with id : %s", digitalServiceUid, id));
        }

        if (!Objects.equals(digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this virtual equipment id: %d", digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        return inVirtualEquipmentMapper.toRest(inVirtualEquipment.get());
    }

    /**
     * Create a new in virtual equipment for a specific digital service.
     *
     * @param digitalServiceUid      the digitalServiceUid.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on virtual equipment created.
     */
    public InVirtualEquipmentRest createInVirtualEquipmentDigitalService(final String digitalServiceUid, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }

        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a virtual equipment.
     *
     * @param digitalServiceUid            the digitalServiceUid.
     * @param id                           the virtual equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InVirtualEquipmentRest updateInVirtualEquipment(final String digitalServiceUid, final Long id, final InVirtualEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no virtual equipment with id : %s", digitalServiceUid, id));
        }

        if (!Objects.equals(digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this virtual equipment id: %d", digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        final InVirtualEquipment objectToUpdate = inVirtualEquipment.get();
        final InVirtualEquipment updates = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inVirtualEquipmentMapper.merge(objectToUpdate, updates);

        inVirtualEquipmentRepository.save(objectToUpdate);
        return inVirtualEquipmentMapper.toRest(objectToUpdate);
    }

    // *** INVENTORY PART ***

    /**
     * Get the virtual equipments list linked to an inventory
     *
     * @param inventoryId the inventory id
     * @return the virtual equipment list.
     */
    public List<InVirtualEquipmentRest> getByInventory(final Long inventoryId) {
        final List<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryId(inventoryId);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipment);
    }

    /**
     * Retrieving a virtual equipment for an inventory and a virtual equipment id.
     *
     * @param inventoryId the inventory id
     * @param id          the virtual equipment id
     * @return InVirtualEquipmentBO
     */
    public InVirtualEquipmentRest getByInventoryAndId(final Long inventoryId, Long id) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no virtual equipment with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inVirtualEquipment.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this virtual equipment id: %d", inventoryId, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        return inVirtualEquipmentMapper.toRest(inVirtualEquipment.get());
    }

    /**
     * Create a new in virtual equipment for a specific inventory.
     *
     * @param inventoryId            the inventory id.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on virtual equipment created.
     */
    public InVirtualEquipmentRest createInVirtualEquipmentInventory(final Long inventoryId, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);

        if (inventory.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory of id : %s, doesn't exist", inventoryId));
        }

        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setInventoryId(inventoryId);
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a virtual equipment.
     *
     * @param inventoryId                  the inventory id.
     * @param id                           the virtual equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InVirtualEquipmentRest updateInVirtualEquipment(final Long inventoryId, final Long id, final InVirtualEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no virtual equipment with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inVirtualEquipment.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this virtual equipment id: %d", inventoryId, inVirtualEquipment.get().getInventoryId(), id));
        }

        final InVirtualEquipment objectToUpdate = inVirtualEquipment.get();
        final InVirtualEquipment updates = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inVirtualEquipmentMapper.merge(objectToUpdate, updates);

        inVirtualEquipmentRepository.save(objectToUpdate);
        return inVirtualEquipmentMapper.toRest(objectToUpdate);
    }

    /**
     * Delete a virtual equipment
     *
     * @param id the virtual equipment id.
     */
    public void deleteInVirtualEquipment(final Long id) {
        inVirtualEquipmentRepository.deleteById(id);
    }

}

