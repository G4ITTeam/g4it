/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Injectable } from "@angular/core";
import {
    createStore,
    emitOnce,
    getStore,
    select,
    setProp,
    setProps,
    withProps,
} from "@ngneat/elf";
import {
    getAllEntitiesApply,
    getEntitiesIds,
    selectActiveEntities,
    selectAllEntities,
    setActiveIds,
    setEntities,
    withActiveIds,
    withEntities,
} from "@ngneat/elf-entities";
import * as utilsCriteria from "src/app/core/utils/criteria";
import { Filter } from "./filter.repository";

export interface Criterias {
    radiation: Criteria;
    acidification: Criteria;
    climate: Criteria;
    resource: Criteria;
    particule: Criteria;
}

export interface Criteria {
    label: string;
    unit: string;
    impacts: Impact[];
}

interface CriteriaProps {
    label: string;
    unit: string;
    computedSelection: ComputedSelection;
}

export interface ChartData<ComputedSelection> {
    [key: string]: ComputedSelection;
}

export interface ComputedSelection {
    acvStep: DataComputed[];
    country: DataComputed[];
    entity: DataComputed[];
    equipment: DataComputed[];
    status: DataComputed[];
}

export interface DataComputed {
    name: string;
    impact: number;
    sip: number;
}

export interface Impact {
    acvStep: string;
    country: string;
    entity: string | null;
    equipment: string | null;
    status: string | null;
    impact: number;
    sip: number;
}

export interface ImpactEntity extends Impact {
    id: number;
}

export interface Datacenter {
    dataCenterName: string;
    physicalEquipmentCount: number;
    country: string | null;
    entity: string | null;
    equipment: string;
    status: string;
    pue: number;
    count?: number;
    avgWeightedPue?: number;
}

export interface PhysicalEquipmentAvgAge {
    organisation: string;
    inventoryDate: string;
    country: string | null;
    type: string;
    nomEntite: string | null;
    statut: string;
    poids: number;
    ageMoyen: number;
    avgWeightedAge?: number;
}

export interface PhysicalEquipmentLowCarbon {
    organisation: string;
    inventoryDate: string;
    paysUtilisation: string | null;
    type: string;
    nomEntite: string | null;
    statut: string;
    quantite: number;
    lowCarbon: boolean;
    pourcentageLowCarbon?: number;
    count?: number;
}

export interface PhysicalEquipmentStats {
    averageAge: PhysicalEquipmentAvgAge[];
    lowCarbon: PhysicalEquipmentLowCarbon[];
}

export interface ApplicationFootprint {
    id?: number;
    criteria: string;
    unit: string;
    criteriaTitle: string;
    impacts: ApplicationImpact[];
}

export interface ApplicationImpact {
    applicationName: string;
    domain: string;
    subDomain: string;
    environment: string;
    equipmentType: string;
    lifeCycle: string;
    impact: number;
    sip: number;
}

export interface ImpactGraph {
    domain: string;
    sipImpact: number;
    unitImpact: number;
    subdomain: string;
    app: string;
    equipment: string;
    environnement: string;
    subdomains: string[];
    apps: string[];
    lifecycle: string;
}

export interface ApplicationCriteriaFootprint {
    criteria: string;
    criteriaTitle: string;
    unit: string;
    impacts: ApplicationCriteriaImpact[];
}

export interface ApplicationCriteriaImpact {
    environment: string;
    equipmentType: string;
    lifeCycle: string;
    impact: number;
    sip: number;
    vmName: string;
    cluster: string;
}

export interface ApplicationGraphPosition {
    domain: string;
    subdomain: string;
    app: string;
    graph: string;
}

const AppGraphPositionStore = createStore(
    { name: "AppGraphPosition" },
    withProps<ApplicationGraphPosition>({
        domain: "",
        subdomain: "",
        app: "",
        graph: "global",
    })
);

utilsCriteria.getCriteriaShortList().forEach((name) =>
    createStore(
        { name },
        withProps<CriteriaProps>({
            label: "",
            unit: "",
            computedSelection: {
                acvStep: [],
                country: [],
                entity: [],
                equipment: [],
                status: [],
            },
        }),
        withEntities<ImpactEntity, "id">(),
        withActiveIds()
    )
);

const particuleStore = getStore("particulate-matter")!;
const acidificationStore = getStore("acidification")!;
const radiationStore = getStore("ionising-radiation")!;
const resourceStore = getStore("resource-use")!;
const climateStore = getStore("climate-change")!;

const DatacenterStatsStore = createStore(
    { name: "DatacentersStats" },
    withProps<Datacenter[]>([]),
    withEntities<Datacenter, "dataCenterName">({
        idKey: "dataCenterName",
        initialValue: [],
    })
);

const PhysicalEquipmentStatsStore = createStore(
    { name: "PhysicalEquipmentsStats" },
    withProps<PhysicalEquipmentStats>({
        lowCarbon: [],
        averageAge: [],
    })
);

const ApplicationFootprintStore = createStore(
    { name: "ApplicationFootprint" },
    withProps<ApplicationFootprint[]>([]),
    withEntities<ApplicationFootprint, "criteria">({
        idKey: "criteria",
        initialValue: [],
    })
);
const ApplicationCriteriaFootprintStore = createStore(
    { name: "ApplicationCriteriaFootprint" },
    withProps<ApplicationCriteriaFootprint[]>([]),
    withEntities<ApplicationCriteriaFootprint, "criteria">({
        idKey: "criteria",
        initialValue: [],
    })
);

@Injectable({ providedIn: "root" })
export class FootprintRepository {
    activeParticuleImpacts$ = particuleStore.pipe(selectActiveEntities());
    activeAcidificationImpacts$ = acidificationStore.pipe(selectActiveEntities());
    activeRadiationImpacts$ = radiationStore.pipe(selectActiveEntities());
    activeResourceImpacts$ = resourceStore.pipe(selectActiveEntities());
    activeClimateImpacts$ = climateStore.pipe(selectActiveEntities());

    particuleComputedSelection$ = particuleStore.pipe(
        select((state) => state.computedSelection)
    );
    acidificationComputedSelection$ = acidificationStore.pipe(
        select((state) => state.computedSelection)
    );
    radiationComputedSelection$ = radiationStore.pipe(
        select((state) => state.computedSelection)
    );
    resourceComputedSelection$ = resourceStore.pipe(
        select((state) => state.computedSelection)
    );
    climateComputedSelection$ = climateStore.pipe(
        select((state) => state.computedSelection)
    );

    datacentersStats$ = DatacenterStatsStore.pipe(selectAllEntities());
    physicalEquipmentStats$ = PhysicalEquipmentStatsStore.pipe(select((state) => state));

    applicationFootprint$ = ApplicationFootprintStore.pipe(selectAllEntities());
    applicationCriteriaFootprint$ = ApplicationCriteriaFootprintStore.pipe(
        selectAllEntities()
    );
    appSelectedDomain$ = AppGraphPositionStore.pipe(select((state) => state.domain));
    appSelectedSubdomain$ = AppGraphPositionStore.pipe(
        select((state) => state.subdomain)
    );
    appSelectedApp$ = AppGraphPositionStore.pipe(select((state) => state.app));
    appSelectedGraph$ = AppGraphPositionStore.pipe(select((state) => state.graph));

    initStores(criterias: Criterias) {
        Object.entries(criterias).forEach(([criteriaName, criteria]) => {
            const store = getStore(criteriaName);
            if (!store) return;

            emitOnce(() => {
                const { label, unit, impacts }: Criteria = criteria;
                store.update(setProp("label", label));
                store.update(setProp("unit", unit));

                const impactsEntities: ImpactEntity[] = impacts.map((impact, index) => ({
                    ...impact,
                    id: index,
                }));

                store.update(setEntities(impactsEntities));
                const entitiesIds = store.query(getEntitiesIds());
                store.update(setActiveIds(entitiesIds));
            });
        });
    }

    updateActiveImpacts(filters: Filter) {
        utilsCriteria
            .getCriteriaShortList()
            .forEach((storeName) => this.updateStoreActiveImpacts(storeName, filters));
    }

    private updateStoreActiveImpacts(storeName: string, filters: Filter) {
        // We know that store is not null as we handle name from within the repository
        const store = getStore(storeName)!;

        const impacts = store.query(
            getAllEntitiesApply({
                filterEntity: (impact) => {
                    let {
                        country = "Empty",
                        entity = "Empty",
                        equipment = "Empty",
                        status = "Empty",
                    } = impact;
                    const countryToCheck = country === null ? "Empty" : country;
                    const countryMatch = filters.countries.includes(countryToCheck);
                    const entityToCheck = entity === null ? "Empty" : entity;
                    const entityMatch = filters.entities.includes(entityToCheck);
                    const equipmentToCheck = equipment === null ? "Empty" : equipment;
                    const equipmentMatch = filters.equipments.includes(equipmentToCheck);
                    const statusToCheck = status === null ? "Empty" : status;
                    const statusMatch = filters.status.includes(statusToCheck);

                    return countryMatch && entityMatch && equipmentMatch && statusMatch;
                },
            })
        );
        const computedSelection: ComputedSelection = {
            acvStep: this.computeData(impacts, "acvStep"),
            country: this.computeData(impacts, "country"),
            entity: this.computeData(impacts, "entity"),
            equipment: this.computeData(impacts, "equipment"),
            status: this.computeData(impacts, "status"),
        };
        emitOnce(() => {
            store.update(setActiveIds(impacts.map((i) => i.id)));
            store.update(setProp("computedSelection", computedSelection));
        });
    }

    private computeData(
        impactsSelected: Impact[],
        property: keyof ComputedSelection
    ): DataComputed[] {
        const summedData: DataComputed[] = [];

        impactsSelected.forEach((impact) => {
            const impactName: string = impact[property] || "Empty";
            const existingData = summedData.find((data) => impactName === data.name);
            if (existingData !== undefined) {
                existingData.impact += impact.impact;
                existingData.sip += impact.sip;
            } else {
                summedData.push({
                    name: impactName,
                    impact: impact.impact,
                    sip: impact.sip,
                });
            }
        });
        return summedData;
    }

    setDatacenters(datacenters: Datacenter[]) {
        DatacenterStatsStore.update(setEntities(datacenters));
    }

    setPhysicalEquipmentStats(
        averageAge: PhysicalEquipmentAvgAge[],
        lowCarbon: PhysicalEquipmentLowCarbon[]
    ): void {
        PhysicalEquipmentStatsStore.update(setProps({ lowCarbon, averageAge }));
    }

    setApplicationFootprint(footprint: ApplicationFootprint[]) {
        ApplicationFootprintStore.update(setEntities(footprint));
    }
    setApplicationCriteriaFootprint(footprint: ApplicationCriteriaFootprint[]) {
        ApplicationCriteriaFootprintStore.update(setEntities(footprint));
    }
    setSelectedDomain(domain: string) {
        AppGraphPositionStore.update(setProp("domain", domain));
    }
    setSelectedSubdomain(subdomain: string) {
        AppGraphPositionStore.update(setProp("subdomain", subdomain));
    }
    setSelectedApp(app: string) {
        AppGraphPositionStore.update(setProp("app", app));
    }
    setSelectedGraph(graph: string) {
        AppGraphPositionStore.update(setProp("graph", graph));
    }

    getValueApplicationFootprint() {
        return ApplicationFootprintStore.getValue().entities;
    }

    getValueAppGraphPositionStore() {
        return AppGraphPositionStore.getValue();
    }
}
