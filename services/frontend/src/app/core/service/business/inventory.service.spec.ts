/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { InventoryService } from "./inventory.service";

describe("InventoryService", () => {
    let httpMock: HttpTestingController;
    let inventoryService: InventoryService;
    let inventoryDate : any = 2;
    let inventory :any ={
        name: "Mar 2024",
        type: "INFORMATION_SYSTEM"
    }

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [InventoryService],
        });
        inventoryService = TestBed.inject(InventoryService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(inventoryService).toBeTruthy();
    });

    it("createInventories() should POST an Inventory", () => {
        inventoryService.createInventory(inventory).subscribe();

        const req = httpMock.expectOne(`inventories`);
        expect(req.request.method).toEqual("POST");

        httpMock.verify();
    });

    it("deleteInventory() should send DELETE request", () => {
        inventoryService.deleteInventory(inventoryDate).subscribe();

        const req = httpMock.expectOne("inventories/" + inventoryDate);

        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });
});
