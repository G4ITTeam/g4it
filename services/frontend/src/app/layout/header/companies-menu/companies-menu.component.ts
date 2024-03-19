/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import {
    ChangeDetectorRef,
    Component,
    ElementRef,
    HostListener,
    Input,
    Renderer2,
    ViewChild,
} from "@angular/core";
import { Subject } from "rxjs";
import { OrganizationData } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-companies-menu",
    templateUrl: "./companies-menu.component.html",
})
export class CompaniesMenuComponent {
    ngUnsubscribe = new Subject<void>();
    isMenuVisible: boolean = false;
    @Input() organizations: OrganizationData[] = [];
    @Input() selectedOrganization: OrganizationData | undefined;

    @ViewChild("menu") menu: ElementRef | undefined;
    @ViewChild("button") button: ElementRef | undefined;

    constructor(
        public userService: UserService,
        private renderer: Renderer2,
        private cdRef: ChangeDetectorRef
    ) {}

    toggleMenu() {
        this.isMenuVisible = !this.isMenuVisible;
    }

    selectCompany(organization: any) {
        this.userService.setUserSubscription(
            organization.subscriber,
            organization.organization
        );
        this.userService.redirectToAllowedPage(
            organization.subscriber.name,
            organization.organization
        );
    }

    @HostListener("document:click", ["$event"])
    handleDocumentClick(event: Event) {
        if (
            this.isMenuVisible &&
            this.menu &&
            this.button &&
            !this.menu.nativeElement.contains(event.target) &&
            event.target !== this.button.nativeElement
        ) {
            this.isMenuVisible = false;
            this.renderer.removeClass(this.menu.nativeElement, "menu-visible");
            this.cdRef.detectChanges();
        }
    }

    chooseTextContrast(bgColor: string): string {
        const color = bgColor.startsWith("#") ? bgColor.substring(1, 7) : bgColor;
        const r = parseInt(color.substring(0, 2), 16); // hexToR
        const g = parseInt(color.substring(2, 4), 16); // hexToG
        const b = parseInt(color.substring(4, 6), 16); // hexToB
        const uicolors = [r / 255, g / 255, b / 255];
        const c = uicolors.map((col) => {
            if (col <= 0.03928) {
                return col / 12.92;
            }
            return Math.pow((col + 0.055) / 1.055, 2.4);
        });
        const L = 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
        return L > 0.179 ? "#000000" : "#FFFFFF";
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
