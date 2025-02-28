/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
export const getLifeCycleList = (): Array<string> => {
    return ["MANUFACTURING", "TRANSPORTATION", "USING", "END_OF_LIFE"];
};

export const getLifeCycleMap = (): Map<string, string> => {
    const result = new Map<string, string>();
    result.set("FABRICATION", "MANUFACTURING");
    result.set("DISTRIBUTION", "TRANSPORTATION");
    result.set("UTILISATION", "USING");
    result.set("FIN_DE_VIE", "END_OF_LIFE");
    return result;
};

export const getLifeCycleMapReverse = (): Map<string, string> => {
    const result = new Map<string, string>();
    result.set("MANUFACTURING", "FABRICATION");
    result.set("TRANSPORTATION", "DISTRIBUTION");
    result.set("USING", "UTILISATION");
    result.set("END_OF_LIFE", "FIN_DE_VIE");
    return result;
};
