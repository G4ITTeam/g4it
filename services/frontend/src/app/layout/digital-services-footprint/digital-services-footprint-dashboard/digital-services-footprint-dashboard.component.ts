/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    computed,
    inject,
    OnDestroy,
    OnInit,
    Signal,
    signal,
} from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { firstValueFrom, takeUntil } from "rxjs";
import {
    DigitalService,
    DigitalServiceCloudImpact,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalResponse,
    DigitalServiceTerminalsImpact,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "src/app/core/interfaces/output.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { OutPhysicalEquipmentsService } from "src/app/core/service/data/in-out/out-physical-equipments.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
import {
    convertToGlobalVision,
    transformOutPhysicalEquipmentsToNetworkData,
    transformOutPhysicalEquipmentstoServerData,
    transformOutPhysicalEquipmentsToTerminalData,
    transformOutVirtualEquipmentsToCloudData,
} from "src/app/core/service/mapper/digital-service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../inventories-footprint/abstract-dashboard";

@Component({
    selector: "app-digital-services-footprint-dashboard",
    templateUrl: "./digital-services-footprint-dashboard.component.html",
})
export class DigitalServicesFootprintDashboardComponent
    extends AbstractDashboard
    implements OnInit, OnDestroy
{
    private digitalServiceStore = inject(DigitalServiceStoreService);
    private outPhysicalEquipmentsService = inject(OutPhysicalEquipmentsService);
    private outVirtualEquipmentsService = inject(OutVirtualEquipmentsService);

    chartType = signal("radial");
    showInconsitencyBtn = false;
    constants = Constants;
    noData = true;
    selectedUnit: string = "Raw";
    selectedCriteria: string = "Global Vision";
    selectedParam: string = "";
    selectedDetailName: string = "";
    selectedDetailParam: string = "";
    showInconsitency = false;
    // barCharChild == true => is the new bar chart generated after clicking on a bar chart
    barChartChild: boolean = false;
    options: EChartsOption = {};
    digitalService: DigitalService = {} as DigitalService;

    title = "";
    content = "";

    impacts: any[] = [];

    globalVisionChartData: DigitalServiceFootprint[] | undefined;

    outPhysicalEquipments: OutPhysicalEquipmentRest[] = [];
    outVirtualEquipments: OutVirtualEquipmentRest[] = [];

    oldNetworkData = signal<DigitalServiceNetworksImpact[]>([]);
    oldCloudData = signal<DigitalServiceCloudImpact[]>([]);
    oldServerData = signal<DigitalServiceServersImpact[]>([]);
    oldTerminalData = signal<DigitalServiceTerminalsImpact[]>([]);

    cloudData = computed(() => {
        if (this.digitalServiceStore.isNewArch()) {
            if (this.outVirtualEquipments === undefined) return [];
            return transformOutVirtualEquipmentsToCloudData(
                this.outVirtualEquipments,
                this.digitalServiceStore.countryMap(),
            );
        } else {
            return this.oldCloudData();
        }
    });

    networkData: Signal<DigitalServiceNetworksImpact[]> = computed(() => {
        if (this.digitalServiceStore.isNewArch()) {
            return transformOutPhysicalEquipmentsToNetworkData(
                this.outPhysicalEquipments,
                this.digitalServiceStore.networkTypes(),
            );
        } else {
            return this.oldNetworkData();
        }
    });

    serverData: Signal<DigitalServiceServersImpact[]> = computed(() => {
        if (this.digitalServiceStore.isNewArch()) {
            return transformOutPhysicalEquipmentstoServerData(
                this.outPhysicalEquipments,
                this.outVirtualEquipments,
                this.digitalServiceStore.serverTypes(),
            );
        } else {
            return this.oldServerData();
        }
    });

    terminalData: Signal<DigitalServiceTerminalsImpact[]> = computed(() => {
        if (this.digitalServiceStore.isNewArch()) {
            return transformOutPhysicalEquipmentsToTerminalData(
                this.outPhysicalEquipments,
                this.digitalServiceStore.terminalDeviceTypes(),
            );
        } else {
            return this.oldTerminalData();
        }
    });

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

    async ngOnInit() {
        this.digitalService = await firstValueFrom(
            this.digitalServicesDataService.digitalService$,
        );
        if (this.digitalService.isNewArch) {
            const [outPhysicalEquipments, outVirtualEquipments] = await Promise.all([
                firstValueFrom(
                    this.outPhysicalEquipmentsService.get(this.digitalService.uid),
                ),
                firstValueFrom(
                    this.outVirtualEquipmentsService.getByDigitalService(
                        this.digitalService.uid,
                    ),
                ),
            ]);
            this.outPhysicalEquipments = outPhysicalEquipments;
            this.outVirtualEquipments = outVirtualEquipments;

            this.retrieveFootprintData(
                this.digitalService.uid,
                this.digitalService.isNewArch,
            );
            if (this.impacts?.length === 1) {
                this.selectedCriteria = this.impacts[0]?.name;
                this.chartType.set("pie");
            }
        } else {
            this.digitalServicesDataService.digitalService$
                .pipe(takeUntil(this.ngUnsubscribe))
                .subscribe((ds: DigitalService) => {
                    this.digitalService = ds;
                    this.retrieveFootprintData(
                        this.digitalService.uid,
                        this.digitalService.isNewArch,
                    );
                    this.digitalService.criteria = ds.criteria;
                });
        }
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

    retrieveFootprintData(uid: string, isNewArch: boolean) {
        this.calculatedCriteriaList = [];
        if (isNewArch) {
            this.globalVisionChartData = convertToGlobalVision(
                this.outPhysicalEquipments,
                this.outVirtualEquipments,
            );
            this.showInconsitencyBtn = this.globalVisionChartData
                .flatMap((footprint) => footprint?.impacts)
                .some((footprint) =>
                    Constants.DATA_QUALITY_ERROR.includes(footprint?.status),
                );

            if (this.globalVisionChartData.length > 0) {
                this.globalVisionChartData[0].impacts.forEach((impact) => {
                    this.calculatedCriteriaList.push(impact.criteria);
                });
            }
            this.initImpacts();
            this.setCriteriaButtons(this.globalVisionChartData);
            if (this.globalVisionChartData.length > 0) {
                this.noData = false;
            } else {
                this.noData = true;
            }
        } else {
            this.digitalServicesService
                .getFootprint(uid)
                .subscribe((footprint: DigitalServiceFootprint[]) => {
                    this.globalVisionChartData = footprint;
                    this.showInconsitencyBtn = this.globalVisionChartData
                        .flatMap((footprint) => footprint?.impacts)
                        .some(
                            (footprint) =>
                                footprint?.status === Constants.DATA_QUALITY_STATUS.error,
                        );
                    if (footprint.length > 0) {
                        footprint[0].impacts.forEach((impact) => {
                            this.calculatedCriteriaList.push(impact.criteria);
                        });
                    }
                    this.initImpacts();
                    this.setCriteriaButtons(footprint);
                    if (footprint.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                    if (this.impacts?.length === 1) {
                        this.selectedCriteria = this.impacts[0]?.name;
                        this.chartType.set("pie");
                    }
                });
            this.digitalServicesService
                .getNetworksIndicators(uid)
                .pipe(takeUntil(this.ngUnsubscribe))
                .subscribe((networkFootprint: DigitalServiceNetworksImpact[]) => {
                    this.oldNetworkData.set(networkFootprint);
                });
            this.digitalServicesService
                .getServersIndicators(uid)
                .pipe(takeUntil(this.ngUnsubscribe))
                .subscribe((serverFootprint: DigitalServiceServersImpact[]) => {
                    this.oldServerData.set(serverFootprint);
                });
            this.digitalServicesService
                .getTerminalsIndicators(uid)
                .pipe(takeUntil(this.ngUnsubscribe))
                .subscribe((terminalFootprint: DigitalServiceTerminalResponse[]) => {
                    this.oldTerminalData.set(
                        this.digitalServicesService.transformTerminalData(
                            terminalFootprint,
                        ),
                    );
                });
            this.digitalServicesService
                .getCloudsIndicators(uid)
                .subscribe((cloudIndicators) => {
                    this.oldCloudData.set(
                        this.digitalServicesService.transformCloudData(
                            cloudIndicators,
                            this.digitalServiceStore.countryMap(),
                        ),
                    );
                });
        }
    }

    handleChartChange(criteria: string) {
        if (this.selectedCriteria === "Global Vision") {
            this.selectedCriteria = criteria;
            this.chartType.set("pie");
            this.barChartChild = false;
        } else if (this.selectedCriteria == criteria) {
            this.selectedCriteria = "Global Vision";
            this.chartType.set("radial");
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
        this.selectedLang = this.translate.currentLang;
        const isBarChart = this.chartType() === "bar";
        const isServer = this.selectedParam === "Server";
        const isCloudService = this.selectedParam === Constants.CLOUD_SERVICE;
        const isBarChartChild = this.barChartChild === true;

        let translationKey: string;

        if (isBarChart) {
            if (isBarChartChild && isServer) {
                translationKey = "digital-services-cards.server-lifecycle.";
            } else if (isBarChartChild && isCloudService) {
                translationKey = "digital-services-cards.cloud-lifecycle.";
            } else {
                translationKey = `digital-services-cards.${this.selectedParam.toLowerCase().replace(/\s+/g, "-")}.`;
            }
        } else {
            const criteriaKey = this.selectedCriteria.toLowerCase().replace(/\s+/g, "-");
            if (
                !Object.keys(this.globalStore.criteriaList()).includes(
                    this.selectedCriteria,
                )
            ) {
                translationKey = `digital-services-cards.${criteriaKey}.`;
            } else {
                return this.translate.instant(
                    this.getTranslationKey(this.selectedCriteria, textType),
                );
            }
        }

        return this.translate.instant(
            `${translationKey}${textType === "digital-services-card-title" ? "title" : "content"}`,
        );
    }

    getTranslationKey(param: string, textType: string) {
        const key = "criteria." + param.toLowerCase().replace(/ /g, "-") + "." + textType;
        return key;
    }

    getTNSTranslation(input: string) {
        return this.translate.instant("digital-services." + input);
    }

    updateInconsistent(event: any): void {
        this.showInconsitencyBtn = event;
    }

    ngOnDestroy() {
        // Clean store data
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
