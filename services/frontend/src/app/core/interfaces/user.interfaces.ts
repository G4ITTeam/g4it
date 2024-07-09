/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Role } from "./roles.interfaces";

export interface User {
    email: string;
    firstName: string;
    lastName: string;
    subscribers: Subscriber[];
}

export interface Subscriber {
    id: number;
    name: string;
    defaultFlag: boolean;
    organizations: Organization[];
    roles: Role[];
}

export interface Organization {
    id: number;
    name: string;
    defaultFlag: boolean;
    status: string;
    roles: Role[];
}

export interface OrganizationData {
    id: number;
    name: string;
    organization?: Organization;
    subscriber?: Subscriber;
    color: string;
}

export interface UserDetails {
    email: string;
    firstName: string;
    id: number;
    lastName: string;
    roles: Role[];
}
