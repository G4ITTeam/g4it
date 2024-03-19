/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, Input } from "@angular/core";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";

@Component({
    selector: "app-information-card-long",
    templateUrl: "./information-card-long.component.html",
})
export class InformationCardLongComponent {
    @Input() title: string = "";
    @Input() content: string = "";

    constructor(private sanitizer: DomSanitizer) {}

    renderHTML(html: string): SafeHtml {
        return this.sanitizer.bypassSecurityTrustHtml(html);
    }
}
