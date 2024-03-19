/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Role } from "./roles.interfaces";

export interface User {
    username: string;
    subscribers: Subscriber[];
}

export interface Subscriber {
    name: string;
    defaultFlag: boolean;
    organizations: Organization[];
}

export interface Organization {
    name: string;
    defaultFlag: boolean;
    roles: Role[];
}

export interface OrganizationData {
    name: string;
    organization?: Organization;
    subscriber?: Subscriber;
    color: string;
}
