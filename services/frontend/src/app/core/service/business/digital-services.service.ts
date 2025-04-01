/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable, inject } from "@angular/core";
import { addDays, formatDate } from "date-fns";
import { Observable, ReplaySubject, firstValueFrom, lastValueFrom } from "rxjs";
import { removeBlankSpaces } from "../../custom-validators/unique-name.validator";
import {
    DSCriteriaRest,
    DigitalService,
    DigitalServiceServerConfig,
    ServerVM,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";
import {
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "../../interfaces/input.interface";
import { DigitalServiceStoreService } from "../../store/digital-service.store";
import { InPhysicalEquipmentsService } from "../data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "../data/in-out/in-virtual-equipments.service";
import { DigitalServicesDataService } from "./../data/digital-services-data.service";

@Injectable({
    providedIn: "root",
})
export class DigitalServiceBusinessService {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);
    private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);

    private readonly panelSubject = new ReplaySubject<boolean>(1);
    panelSubject$ = this.panelSubject.asObservable();

    constructor(private readonly digitalServiceData: DigitalServicesDataService) {}

    closePanel() {
        this.panelSubject.next(false);
    }

    openPanel() {
        this.panelSubject.next(true);
    }

    async submitServerForm(
        server: DigitalServiceServerConfig,
        digitalService: DigitalService,
    ) {
        const physicalEquipment = this.toInPhysicalEquipment(server, digitalService.uid);

        if (server.id) {
            physicalEquipment.id = server.id;
            const isSharedServer = server.mutualizationType === "Shared";
            await firstValueFrom(
                this.inPhysicalEquipmentsService.update(physicalEquipment),
            );
            const existingVms = server.vm.filter((vm) => vm.uid !== "");
            const newVms = server.vm.filter((vm) => vm.uid === "");
            let allVms: InVirtualEquipmentRest[] = [];
            if (existingVms.length && isSharedServer) {
                allVms = existingVms.map((vm) =>
                    this.toInVirtualEquipment(vm, server, digitalService.uid),
                );
            }
            await firstValueFrom(
                this.inVirtualEquipmentsService.updateAllVms(
                    allVms,
                    digitalService.uid,
                    physicalEquipment.id,
                ),
            );
            if (isSharedServer) {
                for (const vm of newVms) {
                    await firstValueFrom(
                        this.inVirtualEquipmentsService.create(
                            this.toInVirtualEquipment(vm, server, digitalService.uid),
                        ),
                    );
                }
            }
        } else {
            await firstValueFrom(
                this.inPhysicalEquipmentsService.create(physicalEquipment),
            );
            for (const vm of server.vm) {
                await firstValueFrom(
                    this.inVirtualEquipmentsService.create(
                        this.toInVirtualEquipment(vm, server, digitalService.uid),
                    ),
                );
            }
        }
        await this.digitalServiceStore.initInPhysicalEquipments(digitalService.uid);
        await this.digitalServiceStore.initInVirtualEquipments(digitalService.uid);
        this.digitalServiceStore.setEnableCalcul(true);
    }

    toInPhysicalEquipment(
        server: DigitalServiceServerConfig,
        digitalServiceUid: string,
    ): InPhysicalEquipmentRest {
        const quantity =
            server.mutualizationType === "Dedicated"
                ? server.quantity * (server.annualOperatingTime! / 8760)
                : 1;

        return {
            name: server.name,
            digitalServiceUid,
            quantity,
            type:
                server.mutualizationType === "Dedicated"
                    ? "Dedicated Server"
                    : "Shared Server",
            model: server.host?.reference,
            datePurchase: "2020-01-01",
            dateWithdrawal: formatDate(
                addDays(new Date("2020-01-01"), 365 * server.lifespan!),
                "yyyy-MM-dd",
            ),
            location: server.datacenter?.location,
            datacenterName: server.datacenter?.name,
            electricityConsumption: server.annualElectricConsumption,
            durationHour: server.annualOperatingTime,
            cpuCoreNumber: server.totalVCpu,
            sizeDiskGb: server.totalDisk,
            description: server.host?.value,
        } as InPhysicalEquipmentRest;
    }

    toInVirtualEquipment(
        vm: ServerVM,
        server: DigitalServiceServerConfig,
        digitalServiceUid: string,
    ) {
        return {
            id: vm.uid ? Number(vm.uid) : undefined,
            digitalServiceUid: digitalServiceUid,
            durationHour: vm.annualOperatingTime,
            infrastructureType: "NON_CLOUD_SERVERS",
            name: vm.name,
            quantity: vm.quantity,
            vcpuCoreNumber: vm.vCpu,
            sizeDiskGb: vm.disk,
            physicalEquipmentName: server.name,
            allocationFactor:
                server.type === "Compute" && server.mutualizationType === "Shared"
                    ? (vm.vCpu / server.totalVCpu!) *
                      (vm.annualOperatingTime / 8760) *
                      vm.quantity
                    : (vm.disk / server.totalDisk!) *
                      (vm.annualOperatingTime / 8760) *
                      vm.quantity,
        } as InVirtualEquipmentRest;
    }

    updateDsCriteria(
        digitalServiceUid: string,
        DSCriteria: DSCriteriaRest,
    ): Observable<DSCriteriaRest> {
        return this.digitalServiceData.updateDsCriteria(digitalServiceUid, DSCriteria);
    }

    getNextAvailableName(existingNames: string[], baseName: string): string {
        const nameSet = new Set(existingNames?.map((n) => removeBlankSpaces(n)));
        let index = 1;
        let newName = `${removeBlankSpaces(baseName)}${String.fromCharCode(64 + index)}`; // Start with "ServerA"

        while (nameSet.has(newName)) {
            index++;
            newName = `${removeBlankSpaces(baseName)}${String.fromCharCode(64 + index)}`; // Increment to "ServerB", "ServerC", etc.
        }
        return `${baseName} ${String.fromCharCode(64 + index)}`;
    }

    async initCountryMap() {
        if (this.digitalServiceStore.countryMap.length > 0) return;

        const boaviztaCountryMap = await lastValueFrom(
            this.digitalServiceData.getBoaviztapiCountryMap(),
        );
        const countryMap: MapString = {};
        for (const key in boaviztaCountryMap) {
            countryMap[boaviztaCountryMap[key]] = key;
        }
        this.digitalServiceStore.setCountryMap(countryMap);
    }
}
