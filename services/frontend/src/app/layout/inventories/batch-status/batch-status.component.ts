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
import { MessageService } from "primeng/api";
import { Subject, firstValueFrom, takeUntil } from "rxjs";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { Constants } from "src/constants";

@Component({
    selector: "app-batch-status",
    templateUrl: "./batch-status.component.html",
})
export class BatchStatusComponent implements OnInit {
    @Input() batchStatusCode: string = "";
    @Input() type: string = "loading";
    cssClass: string = "";
    toolTip: string = "";
    betweenDiv: string = "";
    @Input() createTime: Date | undefined;
    @Input() batchLoading = false;
    @Input() inventoryId: number = 0;
    @Input() inventoryName = "";
    @Input() batchName = "";
    @Input() fileUrl = "";

    selectedOrganization!: string;
    selectedSubscriber!: string;
    ngUnsubscribe = new Subject<void>();

    constructor(
        private fileSystemDataService: FileSystemDataService,
        private router: Router,
        private messageService: MessageService,
        private translate: TranslateService,
        protected userService: UserService,
    ) {}

    ngOnInit(): void {
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

        if (Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(this.batchStatusCode)) {
            this.cssClass = "pi pi-spin pi-spinner icon-running";
            this.toolTip = "Running";
        } else if (this.batchStatusCode === "COMPLETED") {
            this.cssClass = "pi pi-check icon-completed";
            this.toolTip = "Completed";
        } else if (this.batchStatusCode === "FAILED") {
            this.cssClass = "pi pi-times icon-failed";
            this.toolTip = "Failed";
        } else if (this.batchStatusCode === "FAILED_HEADERS") {
            this.cssClass = "pi pi-times icon-failed";
            this.toolTip = "Failed headers";
        } else if (
            this.batchStatusCode === "COMPLETED_WITH_ERRORS" ||
            this.batchStatusCode === "SKIPPED"
        ) {
            this.cssClass = "icon-completed-with-errors";
            this.toolTip = "Completed with errors";
            this.betweenDiv = "!";
        }
    }

    async downloadFile() {
        try {
            const blob: Blob = await firstValueFrom(
                this.fileSystemDataService.downloadResultsFile(
                    this.inventoryId,
                    this.batchName,
                ),
            );
            saveAs(
                blob,
                `g4it_${this.selectedSubscriber}_${this.selectedOrganization}_${this.inventoryId}_rejected-files.zip`,
            );
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
    }
}
