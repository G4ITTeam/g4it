/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    EventEmitter,
    inject,
    Input,
    Output,
    SimpleChanges,
} from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Constants } from "src/constants";

@Component({
    selector: "app-impact-aggregate-infos",
    templateUrl: "./impact-aggregate-infos.component.html",
})
export class ImpactAggregateInfosComponent {
    private translate = inject(TranslateService);

    @Input() displayValue = 0;
    @Input() criteria = "";
    @Input() unit = "";

    @Output() changeUnit = new EventEmitter<string>();

    icon = "";
    unitOfCriteria = "";

    ngOnChanges(changes: SimpleChanges) {
        this.icon = Constants.CRITERIA_ICON[this.criteria];
        this.unitOfCriteria = this.translate.instant(
            `inventories-footprint.critere.${this.criteria}.unite`,
        );
    }

    setUnit(unit: string) {
        this.changeUnit.emit(unit);
    }
}
