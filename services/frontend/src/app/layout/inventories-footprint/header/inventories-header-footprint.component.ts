/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { ConfirmationService, MessageService } from "primeng/api";
import { Subject, firstValueFrom, takeUntil } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import { delay } from "src/app/core/utils/time";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-header-footprint",
    templateUrl: "./inventories-header-footprint.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoriesHeaderFootprintComponent implements OnInit {
    @Input() inventoryId: number = 0;
    @Input() indicatorType: string = "";

    types = Constants.INVENTORY_TYPE;
    batchStatusCode: string | undefined = undefined;
    subscriber = "";
    organization = "";
    sidebarVisible = false;
    inventory: Inventory = {} as Inventory;
    inventoryInterval: any;
    waitingLoop = 10000;
    downloadInProgress = false;

    ngUnsubscribe = new Subject<void>();
    failedStatusCodeList = Constants.EXPORT_BATCH_FAILED_STATUSES;
    inProgressStatusCodeList = Constants.EXPORT_BATCH_IN_PROGRESS_STATUSES;

    selectedOrganization = "";
    selectedSubscriber = "";

    constructor(
        public inventoryRepo: InventoryRepository,
        private inventoryService: InventoryService,
        private confirmationService: ConfirmationService,
        public footprintService: FootprintDataService,
        private translate: TranslateService,
        public router: Router,
        public userService: UserService,
        private messageService: MessageService,
    ) {}

    async ngOnInit() {
        await this.initInventory();
        if (
            this.batchStatusCode &&
            this.batchStatusCode !== Constants.EXPORT_BATCH_GENERATED &&
            this.batchStatusCode !== Constants.EXPORT_REMOVED
        ) {
            this.loopInventories();
        }
        this.userService.currentSubscriber$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((subscriber: Subscriber) => {
                this.selectedSubscriber = subscriber.name;
            });
        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization.name;
            });
    }

    async initInventory() {
        let result = await this.inventoryService.getInventories(this.inventoryId);
        if (result.length > 0) this.inventory = result[0];
        this.batchStatusCode = this.inventory?.exportReport?.batchStatusCode || undefined;
    }

    isGenerated() {
        return Constants.EXPORT_BATCH_GENERATED;
    }

    confirmExport(event: Event) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: this.translate.instant("inventories-footprint.export-message"),
            accept: () => {
                this.exportResult();
            },
        });
    }

    exportResult() {
        this.footprintService.sendExportRequest(this.inventoryId).subscribe((res) => {
            this.batchStatusCode = Constants.EXPORT_BATCH_IN_PROGRESS_STATUSES[0];
        });
        this.loopInventories();
    }

    loopInventories() {
        this.inventoryInterval = setInterval(async () => {
            if (this.batchStatusCode === Constants.EXPORT_BATCH_GENERATED) {
                clearInterval(this.inventoryInterval);
            } else {
                await this.initInventory();
            }
        }, this.waitingLoop);
    }

    changePageToInventories() {
        let [_, _1, subscriber, _2, organization] = this.router.url.split("/");
        return `/subscribers/${subscriber}/organizations/${organization}/inventories`;
    }

    download(event: Event) {
        this.downloadInProgress = true;
        this.downloadFile();
    }
    async downloadFile() {
        try {
            const blob: Blob = await firstValueFrom(
                this.footprintService.downloadExportResultsFile(this.inventoryId),
            );
            saveAs(
                blob,
                `g4it_${this.selectedSubscriber}_${this.selectedOrganization}_${this.inventoryId}_export-result-files.zip`,
            );
            await delay(2000);
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
        this.downloadInProgress = false;
    }

    noteSaveValue(event: any) {
        this.inventory.note = {
            content: event,
        } as Note;

        this.inventoryService
            .updateInventory(this.inventory)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.sidebarVisible = false;
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.save"),
                    sticky: false,
                });
            });
    }

    noteDelete(event: any) {
        this.inventory.note = undefined;
        this.inventoryService
            .updateInventory(this.inventory)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.delete"),
                    sticky: false,
                });
            });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
        clearInterval(this.inventoryInterval);
    }
}
