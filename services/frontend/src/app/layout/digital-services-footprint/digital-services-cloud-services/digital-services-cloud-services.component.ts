/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, signal } from "@angular/core";
import { Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { DigitalServiceCloudServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { MapString } from "src/app/core/interfaces/generic.interfaces";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { InputDataService } from "src/app/core/service/data/input-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-cloud-services",
    templateUrl: "./digital-services-cloud-services.component.html",
    providers: [MessageService],
})
export class DigitalServicesCloudServicesComponent {
    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    cloud: DigitalServiceCloudServiceConfig = {} as DigitalServiceCloudServiceConfig;
    digitalServiceUid = "";

    virtualEquipments = signal<InVirtualEquipmentRest[]>([]);

    cloudServices = computed(() => {
        return this.virtualEquipments().map((server: InVirtualEquipmentRest) =>
            this.toDigitalServiceCloudServiceConfig(
                server,
                this.digitalServiceStore.countryMap(),
            ),
        );
    });

    constructor(
        private inputDataService: InputDataService,
        private digitalServiceStore: DigitalServiceStoreService,
        public userService: UserService,
        private router: Router,
    ) {}

    async ngOnInit() {
        this.digitalServiceUid = this.router.url.split("/")[6];
        this.getCloudServices();
    }

    async getCloudServices() {
        const result = await firstValueFrom(
            this.inputDataService.getVirtualEquipments(this.digitalServiceUid),
        );
        this.virtualEquipments.set(
            result.filter((server) => server.infrastructureType === "CLOUD_SERVICES"),
        );
    }

    setServerCloud(cloud: DigitalServiceCloudServiceConfig, index: number) {
        this.cloud = { ...cloud };
        this.cloud.idFront = index;
    }

    resetCloudServices() {
        this.cloud = {} as DigitalServiceCloudServiceConfig;
    }

    async updateCloudServices(cloud: DigitalServiceCloudServiceConfig) {
        this.cloud.digitalServiceUid = this.digitalServiceUid;
        // Find the index of the cloud server with the matching uid
        let existingCloudIndex = this.cloudServices().findIndex((c) => c.id === cloud.id);
        // If the cloud with the uid exists, update it; otherwise, add the new cloud
        if (
            existingCloudIndex !== -1 &&
            existingCloudIndex !== undefined &&
            this.cloudServices() &&
            cloud.id !== undefined
        ) {
            await lastValueFrom(
                this.inputDataService.update(this.toInVirtualEquipmentRest(cloud)),
            );
        }
        //create it
        else {
            await lastValueFrom(
                this.inputDataService.create(this.toInVirtualEquipmentRest(cloud)),
            );
        }
        this.getCloudServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    async deleteCloudServices(cloud: DigitalServiceCloudServiceConfig) {
        this.cloud.digitalServiceUid = this.digitalServiceUid;
        await lastValueFrom(
            this.inputDataService.delete(this.toInVirtualEquipmentRest(cloud)),
        );
        this.getCloudServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    //mapper
    toInVirtualEquipmentRest(
        cloud: DigitalServiceCloudServiceConfig,
    ): InVirtualEquipmentRest {
        return {
            id: cloud.id,
            digitalServiceUid: cloud.digitalServiceUid,
            name: cloud.name,
            infrastructureType: "CLOUD_SERVICES",
            quantity: cloud.quantity,
            provider: cloud.cloudProvider,
            instanceType: cloud.instanceType,
            location: cloud.location.code,
            durationHour: cloud.annualUsage,
            workload: cloud.averageWorkload / 100,
        };
    }

    toDigitalServiceCloudServiceConfig(
        virtualEq: InVirtualEquipmentRest,
        countryMap: MapString,
    ): DigitalServiceCloudServiceConfig {
        return {
            id: virtualEq.id,
            digitalServiceUid: virtualEq.digitalServiceUid,
            name: virtualEq.name,
            quantity: virtualEq.quantity,
            cloudProvider: virtualEq.provider,
            instanceType: virtualEq.instanceType,
            location: {
                code: virtualEq.location,
                name: countryMap[virtualEq.location] || virtualEq.location,
            },
            annualUsage: virtualEq.durationHour,
            averageWorkload: virtualEq.workload * 100,
        };
    }
}
