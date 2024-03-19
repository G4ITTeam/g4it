/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Injectable } from "@angular/core";
import { createStore, select, setProp, withProps } from "@ngneat/elf";
import { FootprintRepository } from "./footprint.repository";
import { FilterRepository } from "./filter.repository";
import { Observable, combineLatest, map } from "rxjs";

interface EchartsProps {
    critereChart: any[];
    mainChart: any[];
    unitOfCriteria: string;
    isDataInitialized: boolean;
}

const echartsStore = createStore(
    { name: "Echarts" },
    withProps<EchartsProps>({
        critereChart: [],
        mainChart: [],
        unitOfCriteria: "",
        isDataInitialized: false,
    })
);

interface CriteriaImpact {
    impact: number;
    sip: number;
}

@Injectable({ providedIn: "root" })
export class EchartsRepository {
    critereChart$ = echartsStore.pipe(select((state) => state.critereChart));
    unitOfCriteria$ = echartsStore.pipe(select((state) => state.unitOfCriteria));
    mainChart$ = echartsStore.pipe(select((state) => state.mainChart));
    isDataInitialized$ = echartsStore.pipe(select((state) => state.isDataInitialized));

    criteriaImpact$: Observable<CriteriaImpact> = combineLatest([
        this.filterRepo.selectedCriteria$,
        this.footprintRepo.activeClimateImpacts$,
        this.footprintRepo.activeResourceImpacts$,
        this.footprintRepo.activeRadiationImpacts$,
        this.footprintRepo.activeAcidificationImpacts$,
        this.footprintRepo.activeParticuleImpacts$,
    ]).pipe(
        map(
            ([
                criteria,
                climateImpacts,
                resourceImpacts,
                radiationImpacts,
                acidificationImpacts,
                particuleImpacts,
            ]) => {
                switch (criteria) {
                    case "particulate-matter":
                        return this.sumImpact(particuleImpacts);
                    case "acidification":
                        return this.sumImpact(acidificationImpacts);
                    case "ionising-radiation":
                        return this.sumImpact(radiationImpacts);
                    case "resource-use":
                        return this.sumImpact(resourceImpacts);
                    case "climate-change":
                        return this.sumImpact(climateImpacts);
                    default:
                        return { impact: 0, sip: 0 };
                }
            }
        )
    );

    constructor(
        private filterRepo: FilterRepository,
        private footprintRepo: FootprintRepository
    ) {}

    private sumImpact(impacts: CriteriaImpact[]) {
        return impacts.reduce(
            (acc, cur) => ({
                impact: acc.impact + cur.impact,
                sip: acc.sip + cur.sip,
            }),
            { impact: 0, sip: 0 }
        );
    }

    setCritereChart(chart: EchartsProps["critereChart"]) {
        echartsStore.update(setProp("critereChart", chart));
    }

    setMainChart(chart: EchartsProps["mainChart"]) {
        echartsStore.update(setProp("mainChart", chart));
    }

    setUnitOfCriteria(unit: EchartsProps["unitOfCriteria"]) {
        echartsStore.update(setProp("unitOfCriteria", unit));
    }

    setIsDataInitialized(bool: EchartsProps["isDataInitialized"]) {
        echartsStore.update(setProp("isDataInitialized", bool));
    }

    clear() {
        echartsStore.reset();
    }
}
