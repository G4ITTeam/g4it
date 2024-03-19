/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Injectable } from "@angular/core";
import { Observable, lastValueFrom } from "rxjs";
import { sortByProperty } from "sort-by-property";
import { Inventory, CreateInventory } from "src/app/core/interfaces/inventory.interfaces";
import { InventoryRepository } from "../../store/inventory.repository";
import { InventoryDataService } from "../data/inventory-data.service";
import { Constants } from "src/constants";

@Injectable({
    providedIn: "root",
})
export class InventoryService {
    maxReportsSize = 6;

    constructor(
        private inventoryDataService: InventoryDataService,
        private inventoryRepo: InventoryRepository,
    ) {}

    createInventory(creationObj: CreateInventory): Observable<Inventory> {
        return this.inventoryDataService.createInventory(creationObj);
    }

    async getInventories(id?: number): Promise<Inventory[]> {
        let inventories: Inventory[] = [];
        inventories = await lastValueFrom(this.inventoryDataService.getInventories(id));
        if (!inventories || inventories.length === 0) {
            this.inventoryRepo.setInventories([]);
            return [];
        }

        inventories.forEach((inventory: Inventory) => {
            if (inventory.evaluationReports && inventory.evaluationReports.length > 0) {
                inventory.evaluationReports.sort(sortByProperty("createTime", "desc"));
                inventory.evaluationReports = inventory.evaluationReports
                    ? inventory.evaluationReports.slice(0, this.maxReportsSize)
                    : [];
                inventory.lastEvaluationReport = inventory.evaluationReports[0];
                inventory.lastEvaluationReport.progress = parseFloat(
                    inventory.lastEvaluationReport.progressPercentage,
                );
            }
            if (inventory.integrationReports && inventory.integrationReports.length > 0) {
                inventory.integrationReports.sort(sortByProperty("createTime", "desc"));
                inventory.integrationReports = inventory.integrationReports
                    ? inventory.integrationReports.slice(0, this.maxReportsSize)
                    : [];
                inventory.lastIntegrationReport = inventory.integrationReports[0];
            }
            // Format date
            if (inventory.type == Constants.INVENTORY_TYPE.INFORMATION_SYSTEM) {
                const elementsOfDate = inventory.name.split("-");
                inventory.date = new Date(
                    parseInt(elementsOfDate[1]),
                    parseInt(elementsOfDate[0]) - 1,
                );
            }
        });
        this.inventoryRepo.setInventories(inventories);
        return inventories;
    }

    deleteInventory(id: number): Observable<Inventory[]> {
        return this.inventoryDataService.deleteInventory(id);
    }
}
