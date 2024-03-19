/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, Input, Output, EventEmitter } from "@angular/core";

@Component({
    selector: "app-impact-aggregate-infos",
    templateUrl: "./impact-aggregate-infos.component.html",
})
export class ImpactAggregateInfosComponent {
    @Input() displayValue = 0;

    //Peopleeq vs Raw impact
    @Input() selectedUnit = "";

    @Input() selectedCriteria = "";

    @Input() unitOfCriteria = "";

    @Output() selectedUnitChange = new EventEmitter<string>();

    updateSelectedUnite(selectedCriteria: string) {
        this.selectedUnitChange.emit(selectedCriteria);
    }
}
