/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { takeUntil } from "rxjs";
import {
    DigitalService,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalResponse,
    DigitalServiceTerminalsImpact,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import { FilterRepository } from "src/app/core/store/filter.repository";
import { FootprintRepository } from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../inventories-footprint/abstract-dashboard";

@Component({
    selector: "app-digital-services-footprint-dashboard",
    templateUrl: "./digital-services-footprint-dashboard.component.html",
})
export class DigitalServicesFootprintDashboardComponent
    extends AbstractDashboard
    implements OnInit
{
    chartType = "radial";
    noData = true;
    selectedUnit: string = "Raw";
    selectedCriteria: string = "Global Vision";
    selectedParam: string = "";
    selectedDetailName: string = "";
    selectedDetailParam: string = "";
    // barCharChild == true => is the new bar chart generated after clicking on a bar chart
    barChartChild: boolean = false;

    options: EChartsOption = {};
    digitalService: DigitalService = {
        name: "...",
        uid: "",
        creationDate: Date.now(),
        lastUpdateDate: Date.now(),
        lastCalculationDate: null,
        terminals: [],
        servers: [],
        networks: [],
    };

    title = "";
    content = "";

    impacts: any[] = [];

    globalVisionChartData: DigitalServiceFootprint[] | undefined;

    networkData: DigitalServiceNetworksImpact[] = [];
    serverData: DigitalServiceServersImpact[] = [];
    terminalData: DigitalServiceTerminalsImpact[] = [];

    constructor(
        private digitalServicesDataService: DigitalServicesDataService,
        private digitalServicesService: DigitalServiceBusinessService,
        override filterRepo: FilterRepository,
        override footprintRepo: FootprintRepository,
        override echartsRepo: EchartsRepository,
        override translate: TranslateService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
    ) {
        super(
            filterRepo,
            footprintRepo,
            echartsRepo,
            translate,
            integerPipe,
            decimalsPipe,
        );
    }

    ngOnInit() {
        this.digitalServicesDataService.digitalService$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((ds: DigitalService) => {
                this.initImpacts();
                this.digitalService = ds;
                this.retrieveFootprintData(this.digitalService.uid);
            });
    }

    initImpacts(): void {
        this.impacts = Constants.CRITERIAS.map((criteria) => {
            return { name: criteria, title: "", unite: "", raw: null, peopleeq: null };
        });
    }

    retrieveFootprintData(uid: string): void {
        this.digitalServicesService
            .getFootprint(uid)
            .subscribe((footprint: DigitalServiceFootprint[]) => {
                this.globalVisionChartData = footprint;

                this.setCriteriaButtons(footprint);
                if (footprint.length > 0) {
                    this.noData = false;
                } else {
                    this.noData = true;
                }
            });
        this.digitalServicesService
            .getNetworksIndicators(uid)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((networkFootprint: DigitalServiceNetworksImpact[]) => {
                this.networkData = networkFootprint;
            });
        this.digitalServicesService
            .getServersIndicators(uid)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((serverFootprint: DigitalServiceServersImpact[]) => {
                this.serverData = serverFootprint;
            });
        this.digitalServicesService
            .getTerminalsIndicators(uid)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((terminalFootprint: DigitalServiceTerminalResponse[]) => {
                this.terminalData =
                    this.digitalServicesService.transformTerminalData(terminalFootprint);
            });
    }

    handleChartChange(criteria: string) {
        if (this.selectedCriteria === "Global Vision") {
            this.selectedCriteria = criteria;
            this.chartType = "pie";
            this.barChartChild = false;
        } else if (this.selectedCriteria == criteria) {
            this.selectedCriteria = "Global Vision";
            this.chartType = "radial";
            this.barChartChild = false;
        } else if (this.selectedCriteria != criteria) {
            this.selectedCriteria = criteria;
        }
    }

    setCriteriaButtons(globalFootprintData: DigitalServiceFootprint[]): void {
        if (globalFootprintData?.length == 0) {
            this.initImpacts();
            return;
        }

        const criteriaMap = new Map<string, { raw: number; peopleeq: number }>();

        globalFootprintData.forEach((tierData) => {
            tierData.impacts.forEach((impactData) => {
                const { criteria, unitValue, sipValue } = impactData;
                if (criteriaMap.has(criteria)) {
                    criteriaMap.get(criteria)!.raw += unitValue;
                    criteriaMap.get(criteria)!.peopleeq += sipValue;
                } else {
                    criteriaMap.set(criteria, {
                        raw: unitValue,
                        peopleeq: sipValue,
                    });
                }
            });
        });
        this.impacts.forEach((impact) => {
            const criteria = impact.name;
            impact.title = this.translate.instant(`criteria.${criteria}.title`);
            impact.unite = this.translate.instant(`criteria.${criteria}.unite`);
            if (criteriaMap.has(criteria)) {
                impact.raw = criteriaMap.get(criteria)!.raw;
                impact.peopleeq = criteriaMap.get(criteria)!.peopleeq;
            }
        });
    }

    getTitleOrContent(textType: string) {
        let translation: string;
        if (this.chartType == "bar") {
            if (this.barChartChild === true && this.selectedParam === "Server") {
                translation = this.translate.instant(
                    this.getTranslationKey(this.selectedParam + " Lifecycle", textType),
                );
            } else {
                translation = this.translate.instant(
                    this.getTranslationKey(this.selectedParam, textType),
                );
            }
        } else {
            translation = this.translate.instant(
                this.getTranslationKey(this.selectedCriteria, textType),
            );
        }
        return translation;
    }

    getTranslationKey(param: string, textType: string) {
        const key =
            "digital-services-cards." +
            param.toLowerCase().replace(/ /g, "-") +
            "." +
            textType;
        return key;
    }

    getTNSTranslation(input: string) {
        return this.translate.instant("digital-services." + input);
    }
}
