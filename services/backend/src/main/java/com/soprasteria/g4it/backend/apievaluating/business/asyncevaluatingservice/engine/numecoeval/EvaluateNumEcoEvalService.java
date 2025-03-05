/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval;

import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalCalculs;
import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactApplication;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.data.demande.OptionsCalcul;
import org.mte.numecoeval.calculs.domain.data.entree.EquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.data.referentiel.ReferentielEtapeACV;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactApplicationService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementPhysiqueService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementVirtuelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluateNumEcoEvalService {

    @Autowired
    InternalToNumEcoEvalCalculs internalToNumEcoEvalCalculs;

    @Autowired
    ReferentialService referentialNumEcoEvalService;

    @Autowired
    CalculImpactEquipementPhysiqueService calculImpactEquipementPhysiqueService;

    @Autowired
    CalculImpactEquipementVirtuelService calculImpactEquipementVirtuelService;

    @Autowired
    CalculImpactApplicationService calculImpactApplicationService;

    /**
     * Calculate physical equipment impacts with NumEcoEval library
     *
     * @param physicalEquipment the physicalEquipment
     * @param datacenter        the datacenter
     * @param subscriber        the subscriber
     * @param criteria          the criteria
     * @param lifecycleSteps    the lifecycleSteps
     * @param hypotheses        the hypotheses
     * @return the list of impact
     */
    public List<ImpactEquipementPhysique> calculatePhysicalEquipment(final InPhysicalEquipment physicalEquipment,
                                                                     final InDatacenter datacenter,
                                                                     final String subscriber,
                                                                     List<CriterionRest> criteria,
                                                                     List<String> lifecycleSteps,
                                                                     List<HypothesisRest> hypotheses) {

        MatchingItemRest matchingItem = null;

        if (physicalEquipment.getModel() != null) {
            matchingItem = referentialNumEcoEvalService.getMatchingItem(physicalEquipment.getModel(), subscriber);
        }

        ItemTypeRest itemTypeRest = referentialNumEcoEvalService.getItemType(physicalEquipment.getType(), subscriber);

        List<ImpactEquipementPhysique> result = new ArrayList<>(criteria.size() * lifecycleSteps.size());
        LocalDateTime now = LocalDateTime.now();

        for (final String lifecycleStep : lifecycleSteps) {
            for (final CriterionRest criterion : criteria) {

                String itemImpactName = null;

                if (matchingItem == null) {
                    if (itemTypeRest.getRefDefaultItem() != null) {
                        itemImpactName = itemTypeRest.getRefDefaultItem();
                    }
                } else {
                    itemImpactName = matchingItem.getRefItemTarget();
                }

                List<ItemImpactRest> itemImpacts = referentialNumEcoEvalService.getItemImpacts(criterion.getCode(), lifecycleStep,
                        itemImpactName, physicalEquipment.getLocation(), subscriber);

                EquipementPhysique equipementPhysique = internalToNumEcoEvalCalculs.map(physicalEquipment);
                if (datacenter != null) {
                    equipementPhysique.setDataCenter(internalToNumEcoEvalCalculs.map(datacenter));
                }

                DemandeCalculImpactEquipementPhysique demandeCalculImpactEquipementPhysique = DemandeCalculImpactEquipementPhysique.builder()
                        .dateCalcul(now)
                        .equipementPhysique(equipementPhysique)
                        .etape(ReferentielEtapeACV.builder().code(LifecycleStepUtils.getReverse(lifecycleStep)).build())
                        .critere(internalToNumEcoEvalCalculs.map(criterion))
                        .typeItem(internalToNumEcoEvalCalculs.map(itemTypeRest))
                        .correspondanceRefEquipement(internalToNumEcoEvalCalculs.map(matchingItem))
                        .hypotheses(hypotheses.stream()
                                .map(h -> internalToNumEcoEvalCalculs.map(h))
                                .toList())
                        .facteurCaracterisations(itemImpacts.stream()
                                .map(impact -> internalToNumEcoEvalCalculs.map(impact))
                                .toList())
                        .optionsCalcul(new OptionsCalcul("REEL"))
                        .build();

                result.add(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(demandeCalculImpactEquipementPhysique));
            }
        }

        return result;
    }


    /**
     * Calculate virtual equipment impacts with NumEcoEval library
     *
     * @param virtualEquipment             the virtualEquipment
     * @param impactEquipementPhysiqueList the impactEquipementPhysiqueList
     * @param virtualEquipmentNumber       the virtualEquipmentNumber
     * @param totalVcpuNumber              the totalVcpuNumber
     * @param totalStorage                 the totalStorage
     * @return the list of impact
     */
    public List<ImpactEquipementVirtuel> calculateVirtualEquipment(final InVirtualEquipment virtualEquipment,
                                                                   final List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                                                   Integer virtualEquipmentNumber,
                                                                   Integer totalVcpuNumber,
                                                                   Double totalStorage) {

        if (impactEquipementPhysiqueList.isEmpty()) return new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        return impactEquipementPhysiqueList.stream()
                .map(impact -> calculImpactEquipementVirtuelService.calculerImpactEquipementVirtuel(
                        DemandeCalculImpactEquipementVirtuel.builder()
                                .dateCalcul(now)
                                .equipementVirtuel(internalToNumEcoEvalCalculs.map(virtualEquipment))
                                .nbEquipementsVirtuels(virtualEquipmentNumber)
                                .nbTotalVCPU(totalVcpuNumber)
                                .stockageTotalVirtuel(totalStorage)
                                .impactEquipement(impact)
                                .build()))
                .toList();

    }

    /**
     * Calculate application impacts with NumEcoEval library
     *
     * @param application                 the application
     * @param impactEquipementVirtuelList the impactEquipementVirtuelList
     * @param applicationNumber           the applicationNumber
     * @return the list of impact
     */
    public List<ImpactApplication> calculateApplication(final InApplication application,
                                                        final List<ImpactEquipementVirtuel> impactEquipementVirtuelList,
                                                        Integer applicationNumber) {

        if (impactEquipementVirtuelList.isEmpty()) return new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        return impactEquipementVirtuelList.stream()
                .map(impact -> calculImpactApplicationService.calculImpactApplicatif(
                        DemandeCalculImpactApplication.builder()
                                .dateCalcul(now)
                                .application(internalToNumEcoEvalCalculs.map(application))
                                .nbApplications(applicationNumber)
                                .impactEquipementVirtuel(impact)
                                .build()))
                .toList();

    }

    /**
     * Calculate the total vcpuCoreNumber of virtual equipment lists
     * Sum field vcpuCoreNumber
     *
     * @param virtualEquipments virtual equipment list
     * @return total vcpuCoreNumber
     */
    public Double getTotalVcpuCoreNumber(List<InVirtualEquipment> virtualEquipments) {
        Double totalVCPU = null;
        if (virtualEquipments.stream().noneMatch(vm -> vm.getVcpuCoreNumber() == null || vm.getVcpuCoreNumber() == 0)) {
            totalVCPU = virtualEquipments.stream().mapToDouble(InVirtualEquipment::getVcpuCoreNumber).sum();
        }

        return totalVCPU;
    }

    /**
     * Calculate the total disk size of virtual equipment lists
     * Sum field storage
     *
     * @param virtualEquipments virtual equipment list
     * @return le total de capaciteStockage
     */
    public Double getTotalDiskSize(List<InVirtualEquipment> virtualEquipments) {
        Double totalStorage = null;
        if (virtualEquipments.stream().noneMatch(vm -> vm.getSizeDiskGb() == null || vm.getSizeDiskGb() == 0)) {
            totalStorage = virtualEquipments.stream().mapToDouble(InVirtualEquipment::getSizeDiskGb).sum();
        }
        return totalStorage;
    }
}
