/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnDestroy } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Subject } from "rxjs";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import { FilterRepository } from "src/app/core/store/filter.repository";
import { FootprintRepository } from "src/app/core/store/footprint.repository";

@Component({
    template: "",
})
export class AbstractDashboard implements OnDestroy {
    ngUnsubscribe = new Subject<void>();
    dimensions = ["acvStep", "country", "entity", "equipment", "status"];

    constructor(
        public filterRepo: FilterRepository,
        public footprintRepo: FootprintRepository,
        public echartsRepo: EchartsRepository,
        protected translate: TranslateService
    ) {}

    existingTranslation(param: string, view: string) {
        const key = view + "." + param;
        return this.translate.instant(key) === key ? param : this.translate.instant(key);
    }

    ngOnDestroy() {
        // Clean store data
        this.echartsRepo.clear();
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    getCriteriaTranslation(input: string) {
        return this.translate.instant(
            "criteria." + input.toLowerCase().replace(/\s+/g, "-") + ".title"
        );
    }
}
