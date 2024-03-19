/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
export interface Inventory {
    id: number;
    type?: string;
    name: string;
    simulationName: string;
    creationDate: Date;
    lastUpdateDate: Date;
    organization: string;
    dataCenterCount: number;
    physicalEquipmentCount: number;
    virtualEquipmentCount: number;
    applicationCount: number;
    integrationReports: IntegrationReport[];
    evaluationReports: EvaluationReport[];
    date?: Date;
    lastEvaluationReport?: EvaluationReport;
    lastIntegrationReport?: IntegrationReport;
}

export interface CreateInventory {
    type: string;
    name: string;
}

export interface IntegrationReport {
    batchStatusCode: string;
    createTime: Date;
    endTime: Date;
    batchName: string;
    resultFileUrl: string;
    resultFileSize: number;
}

export interface EvaluationReport {
    batchStatusCode: string;
    createTime: Date;
    endTime: Date;
    batchName: string;
    progressPercentage: string;
    progress?: any;
}

export interface EvaluationBody {
    inventoryId: number;
    organization: string;
}

export interface IntegrationBatchLaunchDetail {
    inventoryId: number;
    time: Date;
}
