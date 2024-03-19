/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { DigitalServiceServerConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-side-panel-create-server",
    templateUrl: "./side-panel-create-server.component.html",
    providers:[MessageService]
})
export class SidePanelCreateServerComponent implements OnInit {
    oldType: string = "";
    oldMutualisationType: string = "";

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

    serverForm = this._formBuilder.group({
        name: ["", Validators.required],
        mutualizationType: ["", Validators.required],
        type: ["", Validators.required],
    });

    constructor(
        private digitalService: DigitalServiceBusinessService,
        private _formBuilder: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        public userService:UserService
    ) {}

    ngOnInit(): void {
        this.digitalService.serverFormSubject$.subscribe((res) => {
            this.server = res;
            this.oldType = res.type;
            this.oldMutualisationType = res.mutualizationType;
            if (this.server.uid !== "") {
                this.serverForm.get("mutualizationType")?.disable();
                this.serverForm.get("type")?.disable();
            } else {
                this.serverForm.get("mutualizationType")?.enable();
                this.serverForm.get("type")?.enable();
            }
        });
    }

    nextStep() {
        if (
            this.oldMutualisationType !== this.serverForm.value.mutualizationType ||
            this.oldType !== this.serverForm.value.type
        ) {
            this.server.vm = [];
            this.digitalService.setDataInitialized(false);
        }
        if (this.server.uid === "") {
            this.server.mutualizationType = this.serverForm.value.mutualizationType!;
            this.server.type = this.serverForm.value.type!;
        }
        this.server.name = this.serverForm.value.name!;
        this.digitalService.setServerForm(this.server);
        this.router.navigate(["../parameters"], { relativeTo: this.route });
    }

    close() {
        this.digitalService.setServerForm({
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
        this.digitalService.closePanel();
    }
}
