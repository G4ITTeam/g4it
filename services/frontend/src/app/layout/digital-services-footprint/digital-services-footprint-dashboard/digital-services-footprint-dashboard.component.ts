/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnDestroy, OnInit } from "@angular/core";
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
import { GlobalStoreService } from "src/app/core/store/global.store";
import { AbstractDashboard } from "../../inventories-footprint/abstract-dashboard";

@Component({
    selector: "app-digital-services-footprint-dashboard",
    templateUrl: "./digital-services-footprint-dashboard.component.html",
})
export class DigitalServicesFootprintDashboardComponent
    extends AbstractDashboard
    implements OnInit, OnDestroy
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
        criteria: [],
        members: [],
    };

    title = "";
    content = "";

    impacts: any[] = [];

    globalVisionChartData: DigitalServiceFootprint[] | undefined;

    networkData: DigitalServiceNetworksImpact[] = [];
    serverData: DigitalServiceServersImpact[] = [];
    terminalData: DigitalServiceTerminalsImpact[] = [];

    calculatedCriteriaList: string[] = [];

    constructor(
        private digitalServicesDataService: DigitalServicesDataService,
        private digitalServicesService: DigitalServiceBusinessService,
        override globalStore: GlobalStoreService,
        override translate: TranslateService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
    ) {
        super(translate, integerPipe, decimalsPipe, globalStore);
    }

    ngOnInit() {
        this.digitalServicesDataService.digitalService$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((ds: DigitalService) => {
                this.digitalService = ds;
                this.retrieveFootprintData(this.digitalService.uid);
                this.digitalService.criteria = ds.criteria;
                if (this.impacts?.length === 1) {
                    this.selectedCriteria = this.impacts[0]?.name;
                    this.chartType = "pie";
                }
            });
    }

    initImpacts(): void {
        this.selectedLang = this.translate.currentLang;
        const criteriaKeys = Object.keys(this.globalStore.criteriaList());
        this.impacts = (
            this.calculatedCriteriaList.length > 0
                ? criteriaKeys.filter((criteria) =>
                      this.calculatedCriteriaList.includes(criteria),
                  )
                : criteriaKeys
        ).map((criteria) => {
            return { name: criteria, title: "", unite: "", raw: null, peopleeq: null };
        });
    }

    retrieveFootprintData(uid: string): void {
        this.calculatedCriteriaList = [];
        this.digitalServicesService
            .getFootprint(uid)
            .subscribe((footprint: DigitalServiceFootprint[]) => {
                this.globalVisionChartData = footprint;
                const firstTierData = footprint[0];
                firstTierData.impacts.forEach((impact) => {
                    this.calculatedCriteriaList.push(impact.criteria);
                });
                this.initImpacts();
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
        this.selectedLang = this.translate.currentLang;
        if (this.chartType == "bar") {
            if (this.barChartChild === true && this.selectedParam === "Server") {
                if (textType === "digital-services-card-title") {
                    translation = this.translate.instant(
                        "digital-services-cards.server-lifecycle.title",
                    );
                } else {
                    translation = this.translate.instant(
                        "digital-services-cards.server-lifecycle.content",
                    );
                }
            } else {
                if (textType === "digital-services-card-title") {
                    translation = this.translate.instant(
                        "digital-services-cards.server.title",
                    );
                } else {
                    translation = this.translate.instant(
                        "digital-services-cards.server.content",
                    );
                }
            }
        } else {
            if (
                !Object.keys(this.globalStore.criteriaList()).includes(
                    this.selectedCriteria,
                )
            ) {
                if (textType === "digital-services-card-title") {
                    translation = this.translate.instant(
                        "digital-services-cards." +
                            this.selectedCriteria.toLowerCase().replace(/\s+/g, "-") +
                            ".title",
                    );
                } else {
                    translation = this.translate.instant(
                        "digital-services-cards." +
                            this.selectedCriteria.toLowerCase().replace(/\s+/g, "-") +
                            ".content",
                    );
                }
            } else {
                translation = this.translate.instant(
                    this.getTranslationKey(this.selectedCriteria, textType),
                );
            }
        }
        return translation;
    }

    getTranslationKey(param: string, textType: string) {
        const key = "criteria." + param.toLowerCase().replace(/ /g, "-") + "." + textType;
        return key;
    }

    getTNSTranslation(input: string) {
        return this.translate.instant("digital-services." + input);
    }

    ngOnDestroy() {
        // Clean store data
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
