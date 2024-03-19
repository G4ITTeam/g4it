/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { NgxSpinnerService } from "ngx-spinner";
import { ConfirmationService, MessageService } from "primeng/api";
import { Subject } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { Constants } from "src/constants";
import { sortByProperty } from "sort-by-property";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-inventories",
    templateUrl: "./inventories.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoriesComponent implements OnInit {
    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    id: number = 0;
    name: any = "";
    inventories: Map<string, Inventory[]> = new Map();
    inventoriesForSimulationsAll: Inventory[] = [];
    inventoriesOpen: Set<number> = new Set();
    inventoryBlocksOpen: Set<string> = new Set();
    filterMode: any;
    ngUnsubscribe = new Subject<void>();
    inventoryInterval: any;
    inventoriesToReload: Set<number> = new Set();
    waitingLoop = 10000;
    doLoop = true;
    enableSearchField = true;
    searchFieldTouched = true;

    constructor(
        private inventoryService: InventoryService,
        public router: Router,
        private spinner: NgxSpinnerService,
        public userService:UserService
    ) {}

    async ngOnInit() {
        this.inventoriesOpen = localStorage.getItem("inventoriesOpen")
            ? new Set(
                  localStorage
                      .getItem("inventoriesOpen")
                      ?.split(",")
                      .filter((v) => v !== "NaN")
                      .map((v) => parseInt(v)),
              )
            : new Set();

        if (localStorage.getItem("inventoryBlocksOpen") == null) {
            this.inventoryBlocksOpen = new Set([
                Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
                Constants.INVENTORY_TYPE.SIMULATION,
            ]);
            this.updateLocalStorageBlock();
        } else {
            this.inventoryBlocksOpen = new Set(
                localStorage.getItem("inventoryBlocksOpen")?.split(","),
            );
        }

        await this.reloadInventories();
        if (this.doLoop) {
            this.loopLoadInventories();
        }
    }

    loopLoadInventories() {
        this.inventoryInterval = setInterval(async () => {
            if (this.inventoriesToReload.size === 0) {
                clearInterval(this.inventoryInterval);
            } else {
                for (const id of this.inventoriesToReload) {
                    await this.reloadInventory(id);
                }
            }
        }, this.waitingLoop);
    }

    async reloadInventoriesAndLoop(id: number) {
        await this.reloadInventories();
        this.inventoriesOpen.clear();
        this.inventoriesOpen.add(id);
        this.updateLocalStorage();
        this.loopLoadInventories();
    }

    async reloadInventoryAndLoop(id: number) {
        this.inventoriesToReload.add(id);
        await this.reloadInventory(id);
        this.loopLoadInventories();
    }

    async reloadInventory(id: number) {
        let result = await this.inventoryService.getInventories(id);

        const inventory = result[0];

        if (inventory.type) {
            const index = this.inventories
                .get(inventory.type)
                ?.findIndex((inventory) => inventory.id === id);

            if (index !== undefined && index > -1) {
                this.inventories.get(inventory.type)![index] = inventory;
            }
        }

        this.setInventoryToReload(inventory);
    }

    async reloadInventories() {
        this.spinner.show();

        const allInventories = await this.inventoryService.getInventories();

        this.inventoriesToReload = new Set();
        allInventories.forEach((inventory) => {
            this.setInventoryToReload(inventory);
        });

        this.inventoriesForSimulationsAll = allInventories
            .filter(
                (inventory: Inventory) =>
                    inventory.type === Constants.INVENTORY_TYPE.SIMULATION,
            )
            .sort(sortByProperty("name", "asc"));

        this.inventories.set(
            Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            allInventories
                .filter(
                    (inventory: Inventory) =>
                        inventory.type === Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
                )
                .sort(sortByProperty("date", "desc")),
        );
        this.inventories.set(Constants.INVENTORY_TYPE.SIMULATION, [
            ...this.inventoriesForSimulationsAll,
        ]);

        this.spinner.hide();
    }

    setInventoryToReload(inventory: Inventory) {
        let doAddIntegration = false;
        let doAddEvaluation = false;

        if (inventory.lastIntegrationReport) {
            doAddIntegration =
                !Constants.INTEGRATION_BATCH_COMPLETED_FAILED_STATUSES.includes(
                    inventory.lastIntegrationReport.batchStatusCode,
                );
        }

        if (inventory.lastEvaluationReport) {
            doAddEvaluation =
                !Constants.EVALUATION_BATCH_COMPLETED_FAILED_STATUSES.includes(
                    inventory.lastEvaluationReport.batchStatusCode,
                );
        }

        if (doAddIntegration || doAddEvaluation) {
            this.inventoriesToReload.add(inventory.id);
        } else if (!doAddIntegration && !doAddIntegration) {
            this.inventoriesToReload.delete(inventory.id);
        }
    }

    openSidebarForUploadInventory(id: number) {
        this.sidebarVisible = true;
        this.sidebarPurpose = "upload";
        this.id = id;

        for (let [key, value] of this.inventories) {
            const name = value.find((inventory) => inventory.id === id)?.name;
            if (name) {
                this.name = name;
                break;
            }
        }
    }

    trackByFn(index: any) {
        return index;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
        clearInterval(this.inventoryInterval);
    }

    searchList() {
        this.searchFieldTouched = true;
        if (this.filterMode) {
            const filteredData = this.inventoriesForSimulationsAll.filter((value) => {
                const searchStr = this.filterMode.toLowerCase();
                return value.name.toLowerCase().includes(searchStr);
            });
            this.inventories.set(Constants.INVENTORY_TYPE.SIMULATION, filteredData);
        } else {
            this.inventories.set(Constants.INVENTORY_TYPE.SIMULATION, [
                ...this.inventoriesForSimulationsAll,
            ]);
        }
    }

    onClick($event: MouseEvent) {
        $event.stopPropagation();
    }

    openTab(event: any, inventoryType: string) {
        this.enableSearchField = true;
        this.inventoryBlocksOpen.add(inventoryType);
        this.updateLocalStorageBlock();
    }

    closeTab(event: any, inventoryType: string) {
        this.inventoryBlocksOpen.delete(inventoryType);
        this.enableSearchField = false;
        this.updateLocalStorageBlock();
    }

    childOpenTab(event: any) {
        this.inventoriesOpen.add(event);
        this.updateLocalStorage();
    }

    childCloseTab(event: any) {
        this.inventoriesOpen.delete(event);
        this.updateLocalStorage();
    }

    updateLocalStorage() {
        localStorage.setItem("inventoriesOpen", [...this.inventoriesOpen].join(","));
    }
    updateLocalStorageBlock() {
        localStorage.setItem(
            "inventoryBlocksOpen",
            [...this.inventoryBlocksOpen].join(","),
        );
    }
}
