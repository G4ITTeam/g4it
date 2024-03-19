/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { Router, Routes } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NgxSpinnerModule } from "ngx-spinner";
import { SharedModule } from "../core/shared/shared.module";
import { HeaderComponent } from "./header/header.component";
import { InventoriesComponent } from "./inventories/inventories.component";
import { LayoutComponent } from "./layout.component";
import { MessageService } from "primeng/api";

const routes: Routes = [
    {
        path: "",
        component: LayoutComponent,
        children: [
            {
                path: "inventories",
                component: InventoriesComponent,
            },
        ],
    },
];

describe("LayoutComponent", () => {
    let component: LayoutComponent;
    let fixture: ComponentFixture<LayoutComponent>;
    let template: HTMLElement;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [LayoutComponent, HeaderComponent],
            imports: [
                RouterTestingModule.withRoutes(routes),
                BrowserAnimationsModule,
                NgxSpinnerModule,
                HttpClientTestingModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService, MessageService],
        }).compileComponents();
        router = TestBed.inject(Router);
        fixture = TestBed.createComponent(LayoutComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
        template = fixture.nativeElement;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
