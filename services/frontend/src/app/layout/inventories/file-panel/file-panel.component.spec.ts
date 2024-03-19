/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CommonModule } from "@angular/common";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA, Injectable } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { Message, MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";
import { Observable, of } from "rxjs";
import { LoadingFile } from "src/app/core/interfaces/file-system.interfaces";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FilePanelComponent } from "./file-panel.component";

@Injectable()
class FileSystemServiceMock extends FileSystemDataService {
    override postFileSystemUploadCSV(
        inventoryId: any,
        formData: FormData
    ): Observable<any> {
        return of({
            APPLICATION: ["C:\\tmp\\input\\application_15-06-2023 10-54.csv"],
            DATACENTER: ["C:\\tmp\\input\\datacenter_15-06-2023 10-54.csv"],
            EQUIPEMENT_PHYSIQUE: [
                "C:\\tmp\\input\\equipementPhysique_15-06-2023 10-54.csv",
            ],
            EQUIPEMENT_VIRTUEL: [
                "C:\\tmp\\input\\equipementVirtuel_15-06-2023 10-54.csv",
            ],
        });
    }
}

@Injectable()
class LoadingServiceMock extends LoadingDataService {
    override launchLoading(
        fileList: LoadingFile[],
        inventoryId: any
    ): Observable<number> {
        return of(0);
    }
}

@Injectable()
class InventoryServiceMock extends InventoryDataService {
    override createInventory(inventoryId: any): Observable<any> {
        return of("item");
    }
}

@Injectable()
class MessageServiceMock extends MessageService {
    override add(message: Message): void {}
}

describe("FilePanelComponent", () => {
    let component: FilePanelComponent;
    let fixture: ComponentFixture<FilePanelComponent>;
    let template: HTMLElement;
    let inventoryId:  any = 2;
    let loadingService: LoadingDataService;
    let fileSystemService: FileSystemDataService;
    let inventoryService: InventoryDataService;
    let messageService: MessageService;
    let file: File = new File([], "test.txt");

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FilePanelComponent],
            imports: [
                HttpClientTestingModule,
                SharedModule,
                CommonModule,
                ReactiveFormsModule,
                HttpClientTestingModule,
                FileUploadModule,
                DropdownModule,
                ButtonModule,
                FormsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                {
                    provide: LoadingDataService,
                    useClass: LoadingServiceMock,
                },
                {
                    provide: FileSystemDataService,
                    useClass: FileSystemServiceMock,
                },
                {
                    provide: InventoryDataService,
                    useClass: InventoryServiceMock,
                },
                {
                    provide: MessageService,
                    useClass: MessageServiceMock,
                },
            ],

            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(FilePanelComponent);
        loadingService = TestBed.inject(LoadingDataService);
        fileSystemService = TestBed.inject(FileSystemDataService);
        inventoryService = TestBed.inject(InventoryDataService);
        messageService = TestBed.inject(MessageService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should add component on addComponent", () => {
        component.arrayComponents = [];
        component.addComponent();
        expect(component.arrayComponents.length).toBe(1);
    });

    it("should delete formData on file delete", () => {
        component.arrayComponents = [];
        component.addComponent();
        component.deleteComponent(0);
        expect(component.arrayComponents.length).toBe(0);
    });

    it("should update index for all component", () => {
        component.arrayComponents = [];
        component.addComponent();
        component.addComponent();
        expect(component.arrayComponents[0].instance.index).toBe(0);
        expect(component.arrayComponents[1].instance.index).toBe(1);
    });

    it("should submit formData and create new Inventory", () => {
        //Mock service
        spyOn(inventoryService, "createInventory").and.callThrough();
        spyOn(fileSystemService, "postFileSystemUploadCSV").and.callThrough();

        //MockData
        component.name = 'inventory'
        component.purpose = "new";
        component.inventoryId = inventoryId;
        component.addComponent();
        component.arrayComponents.forEach(({ instance }) => {
            instance.file = file;
            instance.type = { value: "DATACENTER", text: "Datacenter" };
        });

        component.submitFormData();

        expect(inventoryService.createInventory).toHaveBeenCalled();
        expect(fileSystemService.postFileSystemUploadCSV).toHaveBeenCalled();
    });

    it("should submit formData if not a new inventory", () => {
        //Mock service
        spyOn(fileSystemService, "postFileSystemUploadCSV").and.callThrough();

        //MockData
        component.addComponent();
        component.name = 'inventory'
        component.arrayComponents.forEach(({ instance }) => {
            instance.file = file;
            instance.type = { value: "DATACENTER", text: "Datacenter" };
        });
        component.inventoryId = inventoryId;
        component.submitFormData();

        expect(fileSystemService.postFileSystemUploadCSV).toHaveBeenCalled();
    });
    it("should return the date at format mm-yyyy", () => {
        let dateTest = new Date();
        dateTest.setMonth(2);
        dateTest.setFullYear(2023);

        component.onSelectToDate(dateTest);
        expect(component.name).toBe("03-2023");
    });

    it("should clear side panel and create 4 new component", () => {
        component.addComponent();
        component.inventoryId = inventoryId;

        component.clearSidePanel();
        expect(component.arrayComponents.length).toBe(4);
    });

    it("should cancel", () => {
        spyOn(component.sidebarVisibleChange, "emit");

        component.close();

        expect(component.sidebarVisibleChange.emit).toHaveBeenCalled();
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
