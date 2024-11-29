export interface InVirtualEquipmentRest {
    id: number;
    name: string;
    inventoryId?: number;
    digitalServiceUid: string;
    datacenterName?: string;
    physicalEquipmentName?: string;
    quantity: number;
    infrastructureType: string;
    instanceType: string;
    type?: string;
    provider: string;
    location: string;
    durationHour: number;
    workload: number;
    electricityConsumption?: number;
    vcpuCoreNumber?: number;
    sizeMemoryMb?: number;
    sizeDiskGb?: number;
    commonFilters?: string[];
    filters?: string[];
    creationDate?: string;
    lastUpdatedDate?: string;
}
