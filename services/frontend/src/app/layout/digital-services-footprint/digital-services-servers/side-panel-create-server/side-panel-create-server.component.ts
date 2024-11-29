/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import {
    DigitalService,
    DigitalServiceServerConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-side-panel-create-server",
    templateUrl: "./side-panel-create-server.component.html",
    providers: [MessageService],
})
export class SidePanelCreateServerComponent implements OnInit {
    oldType: string = "";
    oldMutualisationType: string = "";
    digitalService: DigitalService = {} as DigitalService;
    serverForm!: FormGroup;

    server: DigitalServiceServerConfig = {
        uid: undefined,
        name: "",
        mutualizationType: "",
        type: "",
        quantity: 0,
        datacenter: {
            uid: "",
            name: "",
            location: "",
            pue: 0,
        },
        vm: [],
    };

    constructor(
        private digitalDataService: DigitalServicesDataService,
        private digitalBusinessService: DigitalServiceBusinessService,
        private _formBuilder: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        public userService: UserService,
    ) {}

    ngOnInit(): void {
        this.digitalBusinessService.serverFormSubject$.subscribe((res) => {
            this.server = res;
            this.oldType = res.type;
            this.oldMutualisationType = res.mutualizationType;
        });

        this.digitalDataService.digitalService$.subscribe((digitalServiceResponse) => {
            this.digitalService = digitalServiceResponse;
        });

        const existingNames = this.digitalService.servers
            .filter((s) => (this.server?.uid ? this.server.name !== s.name : true))
            .map((server) => server.name);

        this.serverForm = this._formBuilder.group({
            name: [
                "",
                [
                    Validators.required,
                    uniqueNameValidator(existingNames),
                    noWhitespaceValidator(),
                ],
            ],
            mutualizationType: ["", Validators.required],
            type: ["", Validators.required],
        });

        this.serverForm.get("name")?.markAsDirty();
    }

    nextStep() {
        if (
            this.oldMutualisationType !== this.serverForm.value.mutualizationType ||
            this.oldType !== this.serverForm.value.type
        ) {
            this.server.vm = [];
            this.digitalBusinessService.setDataInitialized(false);
        }
        if (this.server.uid === "") {
            this.server.mutualizationType = this.serverForm.value.mutualizationType!;
            this.server.type = this.serverForm.value.type!;
        }
        this.server.name = this.serverForm.value.name!;
        this.digitalBusinessService.setServerForm(this.server);
        this.router.navigate(["../parameters"], { relativeTo: this.route });
    }

    close() {
        this.digitalBusinessService.setServerForm({
            uid: "",
            name: "",
            mutualizationType: "",
            host: undefined,
            type: "",
            quantity: 0,
            datacenter: {
                uid: "",
                name: "",
                location: "",
                pue: 0,
            },
            vm: [],
        });
        this.digitalBusinessService.closePanel();
    }
}
