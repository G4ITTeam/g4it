export interface Stat {
    label: string;
    value?: number;
    title: string;
    description: string;
}

export interface FootprintCalculated {
    data: string;
    impacts: Impact[];
    total: SumImpact;
}

export interface Impact {
    criteria: string;
    sumSip: number;
    sumImpact: number;
}

export interface SumImpact {
    impact: number;
    sip: number;
}

export interface EchartPieDataItem {
    name: string;
    value: number;
    otherData: OtherDataValue;
}

export interface OtherDataValue {
    name: string;
    value: number;
    percent: number;
}
