/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, Input, OnInit } from "@angular/core";
import { Constants } from 'src/constants';

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
    @Input() resultFileUrl = "";

    ngOnInit(): void {
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
}
