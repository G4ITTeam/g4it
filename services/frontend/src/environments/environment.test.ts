/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
// This file replace the environment.ts file for tests.
// The list of file replacements can be found in `angular.json`.

export const environment = {
    production: false,
    securedEndpoints: ["inventories", "digital-services"],
    apiEndpoints: {
        inventories: "inventories",
        digitalServices: "digital-services",
        users: "users",
        version: "version"
    },
    apiBaseUrl: "",
    protectedApiRouteUrl: "",
    apiAuth: "", // api protection to add auth token in api calls
    msalConfig: {
        auth: {
            clientId: "", // add your client id
            authority: "",
            redirectUri: "",
        },
    },
    apiConfig: {
        scopes: ["user.read"],
        uri: "https://graph.microsoft.com/v1.0/me",
    },
};
