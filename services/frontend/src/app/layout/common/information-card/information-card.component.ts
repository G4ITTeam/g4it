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
    selector: "app-information-card",
    templateUrl: "./information-card.component.html",
})
export class InformationCardComponent {
    @Input() title: string = "";
    @Input() content: string = "";
    @Input() isLong = false;

    constructor(private sanitizer: DomSanitizer) {}

    renderHTML(html: string): SafeHtml {
        return this.sanitizer.bypassSecurityTrustHtml(html);
    }
}
