/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
// This file can be replaced during build by using the `fileReplacements` array.
// If you modify values in this file, it will likely have no effect.
// The list of file replacements can be found in `angular.json`.

export const environment = {
    production: false,
    securedEndpoints: ["inventories", "digital-services"],
    apiEndpoints: {
        inventories: "inventories",
        digitalServices: "digital-services",
        users: "users",
        version: "version",
    },
    apiBaseUrl: "http://localhost:8080",
    protectedApiRouteUrl: "http://localhost:8080/",
    apiAuth: "api://${AZURE_CLIENT_ID}/GreenIT", // api protection to add auth token in api calls
    msalConfig: {
        auth: {
            clientId: "${AZURE_CLIENT_ID}", // add your client id
            authority: "https://login.microsoftonline.com/organizations",
            redirectUri: "http://localhost:4200/",
        },
    },
    apiConfig: {
        scopes: ["user.read"],
        uri: "https://graph.microsoft.com/v1.0/me",
    },
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 *
 * import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
 */
