/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { Constants } from "src/constants";
import { saveAs } from "file-saver";
import { firstValueFrom } from "rxjs";
import { Router } from "@angular/router";
import sanitize from "src/app/core/utils/filename-sanitizer";
import { MessageService } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";
import { UserService } from "src/app/core/service/business/user.service";

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

    subscriber = "";
    organization = "";

    constructor(
        private fileSystemDataService: FileSystemDataService,
        private router: Router,
        private messageService: MessageService,
        private translate: TranslateService,
        protected userService: UserService
    ) {}

    ngOnInit(): void {
        let [_, subscriber, organization] = this.router.url.split("/");
        this.subscriber = subscriber;
        this.organization = organization;

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
                `g4it_${this.subscriber}_${this.organization}_${sanitize(
                    this.inventoryName,
                    {
                        replacement: "-",
                    },
                )}_rejected-files.zip`,
            );
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
    }
}
