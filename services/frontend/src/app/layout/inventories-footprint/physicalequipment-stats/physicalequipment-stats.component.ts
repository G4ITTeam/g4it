/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { Subject, combineLatestWith, takeUntil } from "rxjs";
import { FilterRepository } from "src/app/core/store/filter.repository";
import {
    FootprintRepository,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
} from "src/app/core/store/footprint.repository";

@Component({
    selector: "app-physicalequipment-stats",
    templateUrl: "./physicalequipment-stats.component.html",
})
export class PhysicalequipmentStatsComponent implements OnInit {
    physicalEquipmentsAvgAgeData: PhysicalEquipmentAvgAge[] = [];
    physicalEquipmentsLowImpactData: PhysicalEquipmentLowImpact[] = [];
    physicalEquipmentPourcentageLowImpact = 0;
    physicalEquipmentAvgAge = 0;
    physicalEquipmentTotalCount = 0;
    selectedCountries: string[] = [];
    selectedEntities: string[] = [];
    selectedEquipments: string[] = [];
    selectedStatus: string[] = [];

    ngUnsubscribe = new Subject<void>();

    constructor(
        private filterRepo: FilterRepository,
        public footprintRepo: FootprintRepository
    ) {}

    ngOnInit(): void {
        this.filterRepo.selectedFilters$
            .pipe(
                combineLatestWith(this.footprintRepo.physicalEquipmentStats$),
                takeUntil(this.ngUnsubscribe)
            )
            .subscribe(([filters, stats]) => {
                this.selectedCountries = filters.countries;
                this.selectedEntities = filters.entities;
                this.selectedStatus = filters.status;
                this.selectedEquipments = filters.equipments;
                this.computePhysicalEquipmentAverageAge(stats.averageAge);
                this.computePhysicalEquipmentLowImpact(stats.lowImpact);
            });
    }

    computePhysicalEquipmentAverageAge(physicalEquipments: PhysicalEquipmentAvgAge[]) {
        physicalEquipments = physicalEquipments || [];
        let physicalEquipmentSum = 0;
        let physicalEquipmentCount = 0;
        physicalEquipments.forEach((physicalEquipment) => {
            let { country, nomEntite, type, statut, poids, ageMoyen } = physicalEquipment;
            country = country || "Empty";
            nomEntite = nomEntite || "Empty";
            type = type || "Empty";
            statut = statut || "Empty";
            poids = poids || 0;
            ageMoyen = ageMoyen || 0;
            if (
                this.selectedCountries.indexOf(country) !== -1 &&
                this.selectedEntities.indexOf(nomEntite) !== -1 &&
                this.selectedEquipments.indexOf(type) !== -1 &&
                this.selectedStatus.indexOf(statut) !== -1
            ) {
                physicalEquipmentCount += poids;
                physicalEquipmentSum += poids * ageMoyen;
            }
        });
        if (physicalEquipmentCount === 0) {
            this.physicalEquipmentAvgAge = 0;
        } else {
            this.physicalEquipmentAvgAge = physicalEquipmentSum / physicalEquipmentCount;
        }
    }

    computePhysicalEquipmentLowImpact(
        physicalEquipmentsLowImpact: PhysicalEquipmentLowImpact[]
    ) {
        physicalEquipmentsLowImpact = physicalEquipmentsLowImpact || [];
        let physicalEquipmentTotalCount = 0;
        let lowImpactPhysicalEquipmentCount = 0;
        physicalEquipmentsLowImpact.forEach((physicalEquipment) => {
            let {
                paysUtilisation,
                nomEntite,
                type,
                statut,
                quantite,
                lowImpact: isLowImpact,
            } = physicalEquipment;
            paysUtilisation = paysUtilisation || "Empty";
            nomEntite = nomEntite || "Empty";
            type = type || "Empty";
            statut = statut || "Empty";
            quantite = quantite || 0;
            if (
                this.selectedCountries.indexOf(paysUtilisation) !== -1 &&
                this.selectedEntities.indexOf(nomEntite) !== -1 &&
                this.selectedEquipments.indexOf(type) !== -1 &&
                this.selectedStatus.indexOf(statut) !== -1
            ) {
                physicalEquipmentTotalCount += quantite;
                if (isLowImpact) {
                    lowImpactPhysicalEquipmentCount += quantite;
                }
            }
        });
        if (physicalEquipmentTotalCount === 0) {
            this.physicalEquipmentPourcentageLowImpact = 0;
        } else {
            this.physicalEquipmentPourcentageLowImpact =
                (lowImpactPhysicalEquipmentCount / physicalEquipmentTotalCount) * 100;
        }
        this.physicalEquipmentTotalCount = physicalEquipmentTotalCount;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
