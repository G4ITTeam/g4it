/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import * as TimeUtils from "src/app/core/utils/time";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventory-item",
    templateUrl: "./inventory-item.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoryItemComponent implements OnInit {
    private global = inject(GlobalStoreService);

    @Input() inventory: Inventory = {} as Inventory;
    @Input() open: boolean = false;
    @Output() reloadInventoriesAndLoop: EventEmitter<number> = new EventEmitter();
    @Output() reloadInventoryAndLoop: EventEmitter<number> = new EventEmitter();
    @Output() openSidebarForUploadInventory: EventEmitter<number> = new EventEmitter();
    @Output() openSidebarForNote: EventEmitter<number> = new EventEmitter();
    @Output() openTab: EventEmitter<number> = new EventEmitter();
    @Output() closeTab: EventEmitter<number> = new EventEmitter();

    batchStatusMapping: any = Constants.EVALUATION_BATCH_STATUS_MAPPING;

    constructor(
        private inventoryService: InventoryService,
        private evaluationService: EvaluationDataService,
        private footprintService: FootprintDataService,
        public router: Router,
        private confirmationService: ConfirmationService,
        private translate: TranslateService,
        private route: ActivatedRoute,
        public userService: UserService,
    ) {}

    ngOnInit() {}

    isRunning() {
        if (!this.inventory.lastEvaluationReport) return false;
        return Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(
            this.inventory.lastEvaluationReport.batchStatusCode,
        );
    }

    showEquipment = () =>
        this.inventory.lastEvaluationReport && this.inventory.physicalEquipmentCount > 0;

    showApplication = () =>
        this.inventory.lastEvaluationReport && this.inventory.applicationCount > 0;

    confirmDelete(event: Event) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant("inventories.popup.delete-question")} ${
                this.inventory.name
            } ?
            ${this.translate.instant("inventories.popup.delete-text")}`,
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                this.global.setLoading(true);
                await lastValueFrom(
                    this.footprintService.deleteIndicators(this.inventory.id),
                );
                await lastValueFrom(
                    this.inventoryService.deleteInventory(this.inventory.id),
                );
                this.reloadInventoriesAndLoop.emit();
                this.global.setLoading(false);
            },
        });
    }

    redirectFootprint(redirectTo: string): void {
        if (!this.inventory.lastEvaluationReport) return;

        let uri = undefined;

        if (redirectTo === "equipment" && this.inventory.physicalEquipmentCount > 0) {
            uri = Constants.MUTLI_CRITERIA;
        } else if (redirectTo === "application" && this.inventory.applicationCount > 0) {
            uri = "application";
        }

        if (uri === undefined) return;

        this.router.navigate([`${this.inventory.id}/footprint/${uri}`], {
            relativeTo: this.route,
        });
    }

    launchEstimate(event: Event) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: this.translate.instant("inventories.popup.estimate"),
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                await lastValueFrom(
                    this.evaluationService.launchEstimation(
                        this.inventory.id,
                        this.inventory.organization,
                    ),
                );
                await TimeUtils.delay(2000);
                this.reloadInventoryAndLoop.emit(this.inventory.id);
            },
        });
    }

    isEstimationDisabled() {
        // If there is no physical equipement, disable button
        if (this.inventory.physicalEquipmentCount <= 0) return true;

        // If there is already an integration running
        if (this.inventory.lastIntegrationReport) {
            if (
                Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(
                    this.inventory.lastIntegrationReport?.batchStatusCode,
                )
            )
                return true;
        }
        // If there is already an estimation running
        if (this.inventory.lastEvaluationReport) {
            if (
                Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(
                    this.inventory.lastEvaluationReport?.batchStatusCode,
                )
            )
                return true;
        }

        // Else enable button
        return false;
    }

    openSidebarUploadFile() {
        this.openSidebarForUploadInventory.emit(this.inventory.id);
    }

    openSidebarNote() {
        this.openSidebarForNote.emit(this.inventory.id);
    }

    async onSelectedChange(id: number, event: any) {
        if (event === undefined) return;
        if (event === true) {
            this.openTab.emit(id);
        } else {
            this.closeTab.emit(id);
        }
    }

    trackByFn(index: any) {
        return index;
    }
}
