/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * In Virtual Equipment JPA repository.
 */
@Repository
public interface InVirtualEquipmentRepository extends JpaRepository<InVirtualEquipment, Long> {

    /**
     * Find virtual equipment by the functionally unique fields
     *
     * @param digitalServiceUid digital service Identifier
     * @param id                virtual equipment id
     * @return return a virtual equipment
     */
    Optional<InVirtualEquipment> findByDigitalServiceUidAndId(String digitalServiceUid, Long id);

    /**
     * Find virtual equipments of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceUid(String digitalServiceUid);

    /**
     * Find virtual equipment by the functionally unique fields
     *
     * @param inventoryId inventory id
     * @param id          virtual equipment id
     * @return return a virtual equipment
     */
    Optional<InVirtualEquipment> findByInventoryIdAndId(Long inventoryId, Long id);

    /**
     * Find virtual equipments of one digital service
     *
     * @param inventoryId inventory id
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByInventoryId(Long inventoryId);


    /**
     * delete by digital service uid
     *
     * @param digitalServiceUid the uid
     */
    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndNameIn(Long inventoryId, Set<String> names);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndPhysicalEquipmentNameIn(Long inventoryId, Set<String> names);

    /**
     * Count virtual equipments linked to an inventory
     *
     * @param inventoryId inventory Id
     * @return the sum of quantity
     */
    @Query("select coalesce(sum(quantity), 0) from InVirtualEquipment ev where ev.inventoryId = :inventoryId")
    Long sumQuantityByInventoryId(Long inventoryId);

    List<InVirtualEquipment> findByDigitalServiceUid(final String digitalServiceUid, final Pageable pageable);

    long countByDigitalServiceUid(final String digitalServiceUid);
}
