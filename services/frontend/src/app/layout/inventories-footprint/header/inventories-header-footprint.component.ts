/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { Subject, takeUntil } from "rxjs";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { Constants } from "src/constants";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-inventories-header-footprint",
    templateUrl: "./inventories-header-footprint.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoriesHeaderFootprintComponent implements OnInit {
    selectedInventory: number = 0;
    inventoryName: string = '';
    inventoryType: string = '';
    types = Constants.INVENTORY_TYPE;

    ngUnsubscribe = new Subject<void>();

    constructor(
        public inventoryRepo: InventoryRepository,
        private inventoryService: InventoryService,
        private confirmationService: ConfirmationService,
        public footprintService: FootprintDataService,
        private translate: TranslateService,
        public router: Router,
        public userService:UserService
    ) {}

    ngOnInit(): void {
        this.inventoryRepo.selectedInventory$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((inventoryId: any) => {
                this.selectedInventory = inventoryId || 0;
                this.getInventoryDetails();
            });
    }

    async getInventoryDetails(){
        let result =  await this.inventoryService.getInventories(this.selectedInventory);
        if(result.length > 0){
            this.inventoryName = result[0].name;
            this.inventoryType = result[0].type ?? "";
        }
    }

    confirmExport(event: Event) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: this.translate.instant("inventories-footprint.export-message"),
            accept: () => {
                this.exportResult(this.selectedInventory);
            },
        });
    }

    exportResult(inventoryId: number) {
        this.footprintService.sendExportRequest(inventoryId).subscribe();
    }

    changePageToInventories() {
        let subscriber = this.router.url.split("/")[1];
        let organization = this.router.url.split("/")[2];
        return `/${subscriber}/${organization}/inventories`;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
