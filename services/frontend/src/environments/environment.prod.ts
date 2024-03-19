/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
// Production env variable are replaced before nginx start
export const environment = {
    production: true,
    apiBaseUrl: "$URL_INVENTORY",
    securedEndpoints: ["inventories", "digital-services"],
    apiEndpoints: {
        inventories: "inventories",
        digitalServices: "digital-services",
        users: "users",
        version: "version"
    },
    protectedApiRouteUrl: "$PROTECTED_API_ROUTE_URL",
    apiAuth: "$MSAL_CONFIG_API_AUTH",
    msalConfig: {
        auth: {
            clientId: "$MSAL_CONFIG_CLIENT_ID",
            authority: "$MSAL_CONFIG_AUTHORITY",
            redirectUri: "$MSAL_CONFIG_REDIRECT_URI",
        },
    },
    apiConfig: {
        scopes: ["user.read"],
        uri: "https://graph.microsoft.com/v1.0/me",
    },
};
