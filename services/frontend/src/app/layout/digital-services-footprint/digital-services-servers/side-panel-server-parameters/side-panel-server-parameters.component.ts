/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit, ViewChild } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { lastValueFrom, Subject, takeUntil } from "rxjs";
import {
    DigitalService,
    DigitalServiceServerConfig,
    Host,
    ServerDC,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import SidePanelDatacenterComponent from "../side-panel-add-datacenter/side-panel-datacenter.component";

@Component({
    selector: "side-panel-server-parameters",
    templateUrl: "./side-panel-server-parameters.component.html",
    providers: [MessageService],
})
export class SidePanelServerParametersComponent implements OnInit {
    @ViewChild("childSidePanel", { static: false })
    childSidePanel!: SidePanelDatacenterComponent;
    ngUnsubscribe = new Subject<void>();

    addSidebarVisible: boolean = false;
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

    digitalService: DigitalService = {} as DigitalService;
    totalVmvCpu = 0;
    serverForm = this._formBuilder.group({
        host: [this.server.host, Validators.required],
        datacenter: [{ uid: "", name: "", location: "", pue: 1 }, Validators.required],
        quantity: [this.server.quantity, [Validators.required]],
        vcpu: [0, [Validators.required]],
        disk: [0, [Validators.required]],
        lifespan: [0, [Validators.required]],
        electricityConsumption: [0, [Validators.required]],
        operatingTime: [8760, [Validators.required]],
    });

    hostOptions: Host[] = [];
    indexHostCompute: number = 0;
    indexHostStorage: number = 0;
    datacenterOptions: ServerDC[] = [];
    indexDatacenter: number = 0;
    dataInitialized: boolean = false;
    createLabelKey = "common.add";
    callReferentialHostDatacenter = true;
    serverSubmitInprogress = false;
    constructor(
        private digitalDataService: DigitalServicesDataService,
        private digitalServiceBusiness: DigitalServiceBusinessService,
        private _formBuilder: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        public userService: UserService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.digitalDataService.digitalService$.subscribe((digitalServiceResponse) => {
            this.digitalService = digitalServiceResponse;
            this.digitalServiceBusiness.serverFormSubject$
                .pipe(takeUntil(this.ngUnsubscribe))
                .subscribe(async (res: DigitalServiceServerConfig) => {
                    this.server = { ...res };
                    if (this.callReferentialHostDatacenter) {
                        await this.setHostReferential(res.type);
                    }
                    this.server = { ...res };
                    if (this.callReferentialHostDatacenter) {
                        await this.setDatacenterReferential(res.datacenter);
                    }
                    this.callReferentialHostDatacenter = false;
                    const serverSaved = this.digitalService.servers.find(
                        (r) => r.uid === res.uid,
                    );
                    const typeValueChanged = serverSaved?.type !== res.type;
                    if (
                        ((this.server.uid === "" && !this.dataInitialized) ||
                            typeValueChanged) &&
                        !this.serverSubmitInprogress
                    ) {
                        this.initializeDefaultValue();
                    }
                    if (!typeValueChanged) {
                        this.indexHostStorage = this.hostOptions.findIndex(
                            (x) => x.value === serverSaved?.host?.value,
                        );
                        this.server.host = this.hostOptions[this.indexHostStorage];
                    }

                    this.createLabelKey = this.getCreateLabelKey(this.server);

                    this.verifyValue();
                });
        });

        this.digitalServiceBusiness.dataInitializedSubject$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.dataInitialized = res;
            });
    }

    getCreateLabelKey(server: DigitalServiceServerConfig): string {
        if (server.mutualizationType === "Dedicated" && server.uid === "") {
            return "common.add";
        }
        if (server.mutualizationType === "Dedicated" && server.uid !== "") {
            return "common.save";
        }
        if (server.mutualizationType === "Shared") {
            return "common.next";
        }
        return "common.add";
    }

    async setHostReferential(type: string) {
        const hostReferentials = await lastValueFrom(
            this.digitalDataService.getHostServerReferential(type),
        );
        this.hostOptions = hostReferentials.sort((a, b) =>
            a.value.localeCompare(b.value),
        );
        const indexS = this.hostOptions.findIndex((x) => x.value === `Server ${type} S`);
        const itemS = this.hostOptions.splice(indexS, 1);
        this.hostOptions.splice(0, 0, ...itemS);
        const indexM = this.hostOptions.findIndex((x) => x.value === `Server ${type} M`);
        const itemM = this.hostOptions.splice(indexM, 1);
        this.hostOptions.splice(1, 0, ...itemM);
        const indexL = this.hostOptions.findIndex((x) => x.value === `Server ${type} L`);
        const itemL = this.hostOptions.splice(indexL, 1);
        this.hostOptions.splice(2, 0, ...itemL);
    }

    async setDatacenterReferential(datacenter: ServerDC | undefined) {
        const dcReferentials = await lastValueFrom(
            this.digitalDataService.getDatacenterServerReferential(
                this.digitalService.uid,
            ),
        );
        if (datacenter?.uid === "" && this.dataInitialized) {
            dcReferentials.push(datacenter);
        }
        this.datacenterOptions = dcReferentials;
        this.datacenterOptions = this.datacenterOptions.map(
            this.formatDatacenterDisplayLabel,
        );
        this.server.datacenter = this.datacenterOptions.find(
            (res) => res.uid === datacenter?.uid,
        );
    }

    formatDatacenterDisplayLabel(data: ServerDC): ServerDC {
        return {
            ...data,
            displayLabel: `${data.name} (${data.location} - PUE = ${data.pue})`,
        };
    }
    verifyValue() {
        this.totalVmvCpu = 0;
        const vcputControl = this.serverForm.get("vcpu");
        if (this.server.vm?.length) {
            this.totalVmvCpu = this.server.vm.reduce(
                (acc, vm) => acc + vm.vCpu * vm.quantity,
                0,
            );
            if (
                this.server?.totalVCpu !== null &&
                (this.server?.totalVCpu ?? 0) < this.totalVmvCpu
            ) {
                vcputControl?.setErrors({
                    ...vcputControl?.errors,
                    isValueTooHigh: true,
                });
                vcputControl?.markAsDirty();
            } else {
                delete vcputControl?.errors?.["isValueTooHigh"];
                vcputControl?.updateValueAndValidity();
            }
        } else {
            delete vcputControl?.errors?.["isValueTooHigh"];
            vcputControl?.updateValueAndValidity();
        }
    }

    initializeDefaultValue() {
        if (this.server.type === "Compute") {
            this.indexHostCompute = this.hostOptions.findIndex(
                (x) => x.value === "Server Compute M",
            );
            this.server.host = this.hostOptions[this.indexHostCompute];
        } else if (this.server.type === "Storage") {
            this.indexHostStorage = this.hostOptions.findIndex(
                (x) => x.value === "Server Storage M",
            );
            this.server.host = this.hostOptions[this.indexHostStorage];
        }
        this.changeDefaultValue();
        this.indexDatacenter = this.datacenterOptions.findIndex(
            (x) => x.name === "Default DC",
        );

        this.datacenterOptions = this.datacenterOptions.map(
            this.formatDatacenterDisplayLabel,
        );
        this.server.datacenter = this.datacenterOptions[0];
        this.server.quantity = 1;
        this.server.annualOperatingTime = 8760;
    }

    changeDefaultValue() {
        const indexHost = this.hostOptions.findIndex(
            (x) => x.value === this.server.host!?.value,
        );
        const indexElec = this.server.host!?.characteristic?.findIndex(
            (x) => x.code === "annualElectricityConsumption",
        );
        if (indexElec !== -1) {
            this.serverForm.controls["electricityConsumption"].setValue(
                this.hostOptions[indexHost]?.characteristic[indexElec].value,
            );
            this.server.annualElectricConsumption =
                this.hostOptions[indexHost]?.characteristic[indexElec].value;
        } else {
            this.serverForm.controls["electricityConsumption"].setValue(null);
            this.server.annualElectricConsumption = undefined;
        }
        const indexLifespan = this.server.host!?.characteristic?.findIndex(
            (x) => x.code === "lifespan",
        );
        if (indexLifespan !== -1) {
            this.serverForm.controls["lifespan"].setValue(
                this.hostOptions[indexHost]?.characteristic[indexLifespan].value,
            );
            this.server.lifespan =
                this.hostOptions[indexHost]?.characteristic[indexLifespan].value;
        } else {
            this.serverForm.controls["lifespan"].setValue(null);
            this.server.lifespan = undefined;
        }

        if (this.server.type === "Compute") {
            const indexVcpu = this.server.host!?.characteristic?.findIndex(
                (x) => x.code === "vCPU",
            );
            if (indexVcpu !== -1) {
                this.serverForm.controls["vcpu"].setValue(
                    this.hostOptions[indexHost]?.characteristic[indexVcpu].value,
                );
                this.server.totalVCpu =
                    this.hostOptions[indexHost]?.characteristic[indexVcpu].value;
            } else {
                this.serverForm.controls["vcpu"].setValue(null);
                this.server.totalVCpu = undefined;
            }
        } else if (this.server.type === "Storage") {
            const indexDisk = this.server.host!?.characteristic?.findIndex(
                (x) => x.code === "disk",
            );
            if (indexDisk !== -1) {
                this.serverForm.controls["disk"].setValue(
                    this.hostOptions[indexHost]?.characteristic[indexDisk].value,
                );
                this.server.totalDisk =
                    this.hostOptions[indexHost]?.characteristic[indexDisk].value;
            } else {
                this.serverForm.controls["disk"].setValue(null);
                this.server.totalDisk = undefined;
            }
        }
    }

    async addDatacenter(datacenter: ServerDC) {
        this.datacenterOptions.push(datacenter);
        this.datacenterOptions = this.datacenterOptions.map(
            this.formatDatacenterDisplayLabel,
        );
        this.server.datacenter = this.datacenterOptions.find(
            (res) => res.name === datacenter.name,
        );
    }

    previousStep() {
        this.digitalServiceBusiness.setDataInitialized(true);
        this.digitalServiceBusiness.setServerForm(this.server);
        this.router.navigate(["../create"], { relativeTo: this.route });
    }

    async nextStep() {
        this.serverSubmitInprogress = true;
        this.digitalServiceBusiness.setServerForm(this.server);
        if (this.server.mutualizationType === "Dedicated") {
            this.digitalServiceBusiness.submitServerForm(
                this.server,
                this.digitalService,
            );
            this.close();
        } else if (this.server.mutualizationType === "Shared") {
            this.digitalServiceBusiness.setDataInitialized(true);
            this.router.navigate(["../vm"], { relativeTo: this.route });
        }
        this.serverSubmitInprogress = false;
    }

    close() {
        this.digitalServiceBusiness.setServerForm({
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
        this.digitalServiceBusiness.closePanel();
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
