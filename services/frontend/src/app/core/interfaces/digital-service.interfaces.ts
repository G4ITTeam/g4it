/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Note } from "./note.interface";

export interface DigitalService {
    uid: string;
    name: string;
    creationDate: number;
    lastUpdateDate: number;
    lastCalculationDate: number | null;
    terminals: DigitalServiceTerminalConfig[];
    servers: DigitalServiceServerConfig[];
    networks: DigitalServiceNetworkConfig[];
    note?: Note;
    userId?: number;
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
    lifespan: number;
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
    lifespan: number;
}

export interface NetworkType {
    code: string;
    value: string;
}

export interface DigitalServiceTerminalResponse {
    criteria: string;
    impacts: TerminalImpact[];
}

export interface TerminalImpact {
    acvStep: string;
    country: string;
    description: string;
    numberUsers: number;
    rawValue: number;
    sipValue: number;
    unit: string;
    yearlyUsageTimePerUser: number;
}

export interface DigitalServiceTerminalsImpact {
    criteria: string;
    impactCountry: TerminalsImpact[];
    impactType: TerminalsImpact[];
}

export interface TerminalsImpact {
    rawValue?: any;
    unit?: any;
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
    rawValue?: number;
    unit?: string;
    acvStep: string;
    sipValue: number;
}

export interface ImpactTerminalsACVStep {
    ACVStep: string;
    sipValue: number;
}

export interface ImpactNetworkSipValue {
    unit?: string;
    networkType: string;
    sipValue: number;
    rawValue: number;
}

export interface ImpactSipValue {
    rawValue?: number;
    unit?: string;
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
    displayLabel?: string;
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
