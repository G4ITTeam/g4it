/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
export interface DigitalService {
    uid: string;
    name: string;
    creationDate: number;
    lastUpdateDate: number;
    lastCalculationDate: number | null;
    terminals: DigitalServiceTerminalConfig[];
    servers: DigitalServiceServerConfig[];
    networks: DigitalServiceNetworkConfig[];
}

export interface DigitalServiceServerConfig {
    sumOfVmQuantity?: number;
    uid?: string;
    creationDate?: number;
    name: string;
    mutualizationType: string;
    type: string;
    quantity: number;
    host?: Host;
    datacenter?: ServerDC;
    totalVCpu?: number;
    totalDisk?: number;
    lifespan?: number;
    annualElectricConsumption?: number;
    annualOperatingTime?: number;
    vm?: ServerVM[];
}

export interface DigitalServiceNetworkConfig {
    uid?: string;
    creationDate?: number;
    type: NetworkType;
    yearlyQuantityOfGbExchanged: number;
    idFront?: number;
}

export interface DigitalServiceTerminalConfig {
    uid?: string;
    creationDate?: number;
    type: TerminalsType;
    country: string;
    numberOfUsers: number;
    yearlyUsageTimePerUser: number;
    idFront?: number;
}

export interface DigitalServiceFootprint {
    tier: string;
    impacts: DigitalServiceFootprintImpact[];
}

export interface DigitalServiceFootprintImpact {
    criteria: string;
    sipValue: number;
    unitValue: number;
    unit: string;
}

export interface TerminalsType {
    code: string;
    value: string;
}

export interface NetworkType {
    code: string;
    value: string;
}

export interface DigitalServiceTerminalsImpact {
    criteria: string;
    impactCountry: TerminalsImpact[];
    impactType: TerminalsImpact[];
}

export interface TerminalsImpact {
    name: string;
    totalSipValue: number;
    totalNbUsers: number;
    avgUsageTime: number;
    impact: ImpactTerminalsACVStep[];
}

export interface DigitalServiceNetworksImpact {
    criteria: string;
    impacts: ImpactNetworkSipValue[];
}

export interface DigitalServiceServersImpact {
    criteria: string;
    impactsServer: ServersType[];
}
export interface ServersType {
    serverType: string;
    mutualizationType: string;
    servers: ServerImpact[];
}

export interface ServerImpact {
    name: string;
    totalSipValue: number;
    hostingEfficiency?: string;
    impactVmDisk: ImpactSipValue[];
    impactStep: ImpactACVStep[];
}

export interface ImpactACVStep {
    acvStep: string;
    sipValue: number;
}

export interface ImpactTerminalsACVStep {
    ACVStep: string;
    sipValue: number;
}

export interface ImpactNetworkSipValue {
    networkType: string;
    sipValue: number;
    rawValue: number;
}

export interface ImpactSipValue {
    name: string;
    sipValue: number;
    quantity: number;
}
export interface ServerVM {
    uid: string;
    name: string;
    vCpu: number;
    disk: number;
    quantity: number;
    annualOperatingTime: number;
}

export interface ServerDC {
    uid: string;
    name: string;
    location: string;
    pue: number;
}

export interface Host {
    code: number;
    value: string;
    characteristic: HostCharacteristics[];
}

export interface HostCharacteristics {
    code: string;
    value: number;
}
