/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import {
    Component,
    ComponentRef,
    EventEmitter,
    Input,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
    ViewContainerRef,
} from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService } from "primeng/api";
import { Subject, takeUntil } from "rxjs";
import { FileType, LoadingFile } from "src/app/core/interfaces/file-system.interfaces";
import { Inventory, CreateInventory } from "src/app/core/interfaces/inventory.interfaces";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import { SelectFileComponent } from "./select-file/select-file.component";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Constants } from "src/constants";

@Component({
    selector: "app-file-panel",
    templateUrl: "./file-panel.component.html",
})
export class FilePanelComponent implements OnInit {
    className: string = "default-calendar max-w-full";

    @ViewChild("uploaderContainer", { read: ViewContainerRef })
    uploaderContainer!: ViewContainerRef;
    @Input() purpose: string = "";
    @Input() name: string = ""; // inventoryDate (for IS Type)
    @Input() inventoryId?: number = 0;
    @Input() allSimulations: Inventory[] = [];
    @Input() inventories: Inventory[] = [];

    @Output() sidebarPurposeChange: EventEmitter<any> = new EventEmitter();
    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();
    @Output() reloadInventoriesAndLoop = new EventEmitter<number>();

    public fileTypes: FileType[] = [];
    invalidDates: Date[] = [];
    selectedType: string = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;
    inventoryDates: Date[] = [];
    simulationNames: string[] = [];
    inventoriesForm!: FormGroup;
    inventoryType = Constants.INVENTORY_TYPE;
    ngUnsubscribe = new Subject<void>();

    private readonly uploaderOutpoutHandlerReset$ = new Subject<void>();
    arrayComponents: Array<ComponentRef<SelectFileComponent>> = [];

    constructor(
        private inventoryService: InventoryDataService,
        private filesSystemService: FileSystemDataService,
        private loadingService: LoadingDataService,
        private messageService: MessageService,
        private spinner: NgxSpinnerService,
        private translate: TranslateService,
        private readonly formBuilder: FormBuilder,
    ) {}

    ngOnInit(): void {
        this.fileTypes = [
            {
                value: "DATACENTER",
                text: this.translate.instant("inventories.type.dc"),
            },
            {
                value: "EQUIPEMENT_PHYSIQUE",
                text: this.translate.instant("inventories.type.eq-phys"),
            },
            {
                value: "EQUIPEMENT_VIRTUEL",
                text: this.translate.instant("inventories.type.eq-virt"),
            },
            {
                value: "APPLICATION",
                text: this.translate.instant("inventories.type.app"),
            },
        ];
        this.inventoriesForm = this.formBuilder.group({
            name: ["", [Validators.pattern(/^[^<>]+$/), Validators.maxLength(255)]],
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        this.invalidDates = [];
        this.inventories?.forEach((inventory) => {
            const month = inventory.date!.getMonth();
            let year = inventory.date!.getFullYear();
            for (let day = 1; day < 32; day++) {
                this.invalidDates.push(new Date(year, month, day));
            }
        });
    }

    checkForDuplicate() {
        return this.allSimulations.some(
            (inventory) => inventory.name == this.name?.trim(),
        );
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.fileTypes.forEach((type) => this.addComponent(type));
        }, 500);
    }

    get inventoriesFormControls() {
        return this.inventoriesForm.controls;
    }

    deleteComponent(index: number) {
        this.arrayComponents.at(index)?.destroy();
        this.arrayComponents.splice(index, 1);
        this.arrayComponents.forEach(({ instance }, index) => (instance.index = index));
    }

    addComponent(type = this.fileTypes[0]) {
        const componentRef = this.uploaderContainer.createComponent(SelectFileComponent);
        componentRef.setInput("fileTypes", this.fileTypes);
        componentRef.instance.type = type;
        this.arrayComponents.push(componentRef);
        this.uploaderOutpoutHandlerReset$.next();
        this.arrayComponents.forEach(({ instance }, index) => {
            instance.index = index;
            instance.onDelete
                .asObservable()
                .pipe(takeUntil(this.uploaderOutpoutHandlerReset$))
                .subscribe(() => this.deleteComponent(instance.index));
        });
    }

    submitFormData() {
        this.spinner.show();
        if (this.name === "") {
            this.className = "ng-invalid ng-dirty";
            this.spinner.hide();
            return;
        }
        let formData = new FormData();
        let bodyLoading: LoadingFile[] = [];

        this.arrayComponents.forEach(({ instance }) => {
            const { type, file } = instance;
            if (file) {
                formData.append(type.value, file, file.name);
                bodyLoading.push({
                    name: file.name,
                    type: type.value,
                    metadata: {
                        creationTime: new Date().toString(),
                    },
                });
            }
        });
        if (this.purpose === "new") {
            const creationObj: CreateInventory = {
                name: this.name,
                type: this.selectedType,
            };
            this.inventoryService.createInventory(creationObj).subscribe({
                next: (response) => {
                    this.messageService.add({
                        severity: "success",
                        summary: this.translate.instant(
                            "inventories.creation-successful",
                        ),
                        detail: `${this.translate.instant("inventories.inventory")} ${
                            this.name
                        } ${this.translate.instant("inventories.created")}`,
                    });
                    if (bodyLoading.length !== 0) {
                        this.uploadAndLaunchLoading(formData, bodyLoading, response.id);
                    } else {
                        this.reloadInventoriesAndLoop.emit(response.id);
                        this.close();
                        this.spinner.hide();
                    }
                },
                error: (error) => {
                    this.spinner.hide();
                },
            });
        } else {
            if (bodyLoading.length !== 0) {
                this.uploadAndLaunchLoading(formData, bodyLoading, this.inventoryId);
            } else {
                this.spinner.hide();
            }
        }
    }

    onSelectToDate(date: Date) {
        const monthNumber = `${date.getMonth() + 1}`;
        this.name = `${monthNumber.padStart(2, "0")}-${date.getFullYear()}`;
        this.className = "default-calendar";
    }

    uploadAndLaunchLoading(
        formData: FormData,
        bodyLaunchLoading: LoadingFile[],
        inventoryId: number = 0,
    ) {
        this.filesSystemService.postFileSystemUploadCSV(inventoryId, formData).subscribe({
            next: async () => {
                this.loadingService
                    .launchLoading(bodyLaunchLoading, inventoryId)
                    .subscribe({
                        next: () => {
                            this.sidebarVisibleChange.emit(false);
                            this.reloadInventoriesAndLoop.emit(inventoryId);
                            this.close();
                            this.spinner.hide();
                        },
                        error: () => {
                            this.spinner.hide();
                        },
                    });
            },
            error: () => {
                this.sidebarPurposeChange.emit("upload");
                this.spinner.hide();
            },
        });
    }

    clearSidePanel() {
        this.arrayComponents.forEach((component) => {
            component.destroy();
        });
        this.arrayComponents = [];
        this.fileTypes.forEach((type) => this.addComponent(type));
    }

    close() {
        this.name = "";
        this.sidebarVisibleChange.emit(false);
        this.clearSidePanel();
    }
    clearNameField() {
        this.name = "";
        this.inventoriesForm.get("name")?.reset();
    }
    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
