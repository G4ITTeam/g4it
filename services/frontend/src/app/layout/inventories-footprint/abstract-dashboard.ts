/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Subject } from "rxjs";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    template: "",
})
export class AbstractDashboard {
    ngUnsubscribe = new Subject<void>();
    selectedLang: string = "en";

    constructor(
        protected translate: TranslateService,
        protected integerPipe: IntegerPipe,
        protected decimalsPipe: DecimalsPipe,
        protected globalStore: GlobalStoreService,
    ) {}

    existingTranslation(param: string, view: string, type: string = "series") {
        let key = view + "." + param;
        if (param === "other") {
            key = type === "legend" ? "common.otherLegend" : "common.other";
        }
        return this.translate.instant(key) === key ? param : this.translate.instant(key);
    }

    getCriteriaTranslation(input: string) {
        this.selectedLang = this.translate.currentLang;
        if (!Object.keys(this.globalStore.criteriaList()).includes(input)) {
            return this.translate.instant(
                "criteria-title." + input.toLowerCase().replace(/\s+/g, "-") + ".title",
            );
        } else {
            return this.translate.instant(
                "criteria." + input.toLowerCase().replace(/\s+/g, "-") + ".title",
            );
        }
    }
}
