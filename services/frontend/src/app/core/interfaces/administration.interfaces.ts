import { Role } from "./roles.interfaces";

export interface Subscriber {
    name?: string;
    defaultFlag?: boolean;
    id?: number;
    organizations?: Organization[];
    roles?: Role[];
}

export interface Organization {
    id: number;
    name: string;
    status: string;
    deletionDate: string;
    dataRetentionDay: number | null;
    defaultFlag: boolean;
    roles: Role[];
    uiStatus?: string;
}

export interface OrganizationUpsertRest {
    subscriberId: number;
    name: string;
    status: string | null;
    dataRetentionDay?: number | null;
}

export interface OrganizationWithSubscriber {
    subscriberName: string;
    subscriberId: number;
    organizationName: string;
    organizationId: number;
    roles: Role[];
    displayLabel: string;
}
