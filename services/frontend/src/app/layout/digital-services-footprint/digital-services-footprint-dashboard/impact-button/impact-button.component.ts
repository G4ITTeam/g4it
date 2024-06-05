/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";

@Component({
    selector: "app-impact-button",
    templateUrl: "./impact-button.component.html",
})
export class ImpactButtonComponent implements OnInit {
    @Input() impact: string = "...";
    @Input() impactText: string = "Other";
    @Input() impactUnite: string = "";
    @Input() value: any;
    @Input() selectedCriteria: string = "";
    @Output() selectedCriteriaChange: EventEmitter<any> = new EventEmitter();
    @Input() selectedUnit: string = "";
    impactImage: string = "";

    iconMap: any = {
        "particulate-matter": "factory",
        acidification: "ph",
        "ionising-radiation": "ion",
        "resource-use": "hourglass",
        "climate-change": "climate",
    };

    constructor(private sanitizer: DomSanitizer, private translate: TranslateService) {}

    renderHTML(html: string): SafeHtml {
        return this.sanitizer.bypassSecurityTrustHtml(html);
    }

    ngOnInit(): void {
        if (this.impact === "...") {
            this.impactImage = "assets/images/icons/icon-hourglass.svg";
            this.impactUnite = "N/A";
        }
        this.impactImage = `assets/images/icons/icon-${this.iconMap[this.impact]}.svg`;
    }

    changeCritere(critere: string) {
        this.selectedCriteriaChange.emit(critere);
    }
}
