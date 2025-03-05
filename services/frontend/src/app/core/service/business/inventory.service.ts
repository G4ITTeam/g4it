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
import {
    CreateInventory,
    Inventory,
    InventoryCriteriaRest,
    InventoryUpdateRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { Constants } from "src/constants";
import { InventoryRepository } from "../../store/inventory.repository";
import { InventoryDataService } from "../data/inventory-data.service";

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

    updateInventory(inventory: Inventory): Observable<InventoryUpdateRest> {
        const inventoryRest: InventoryUpdateRest = {
            id: inventory.id,
            name: inventory.name,
            note: inventory.note,
        };
        return this.inventoryDataService.updateInventory(inventoryRest);
    }

    async getInventories(id?: number): Promise<Inventory[]> {
        let inventories: Inventory[] = [];
        inventories = await lastValueFrom(this.inventoryDataService.getInventories(id));
        if (!inventories || inventories.length === 0) {
            this.inventoryRepo.setInventories([]);
            return [];
        }

        inventories.forEach((inventory: Inventory) => {
            this.enrichInventory(inventory);
        });
        this.inventoryRepo.setInventories(inventories);
        return inventories;
    }

    enrichInventory(inventory: Inventory) {
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
        if (inventory.tasks && inventory.tasks.length > 0) {
            inventory.tasks.sort(sortByProperty("creationDate", "desc"));

            const tasksLoading = inventory.tasks
                .filter((t) => t.type === "LOADING")
                .slice(0, this.maxReportsSize);
            const tasksEvaluating = inventory.tasks
                .filter((t) => t.type === "EVALUATING")
                .slice(0, this.maxReportsSize);

            inventory.tasks = inventory.tasks
                ? [...tasksLoading, ...tasksEvaluating]
                : [];

            if (tasksLoading.length > 0) {
                inventory.lastTaskLoading = tasksLoading[0];
                if (inventory.lastTaskLoading) {
                    inventory.lastTaskLoading.progress =
                        +inventory.lastTaskLoading?.progressPercentage.replace("%", "")!;
                }
            }

            if (tasksEvaluating.length > 0) {
                inventory.lastTaskEvaluating = tasksEvaluating[0];
                if (inventory.lastTaskEvaluating) {
                    inventory.lastTaskEvaluating.progress =
                        +inventory.lastTaskEvaluating?.progressPercentage.replace(
                            "%",
                            "",
                        )!;
                }
            }
        }

        // Format date
        if (inventory.type == Constants.INVENTORY_TYPE.INFORMATION_SYSTEM) {
            const elementsOfDate = inventory.name.split("-");
            inventory.date = new Date(
                parseInt(elementsOfDate[1]),
                parseInt(elementsOfDate[0]) - 1,
            );
        }
    }

    deleteInventory(id: number): Observable<Inventory[]> {
        return this.inventoryDataService.deleteInventory(id);
    }

    updateInventoryCriteria(
        inventoryCriteria: InventoryCriteriaRest,
    ): Observable<Inventory> {
        return this.inventoryDataService.updateInventoryCriteria(inventoryCriteria);
    }
}
