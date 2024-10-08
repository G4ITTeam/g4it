/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
const express = require("express");
const router = express.Router();

const jsonFilter = require("../data/inventory-data/filters_anon.json");
const jsonFilterApp = require("../data/inventory-data/filters_application.json");
const jsonDataIndicator = require("../data/inventory-data/indicators_anon.json");
const jsonDataApplicationIndicator = require("../data/inventory-data/indicators_application.json");
const jsonDataCenter = require("../data/inventory-data/datacenter_anon.json");
const jsonDataApplicationRadiation = require("../data/inventory-data/indicators_application_radiation.json");
const jsonDataApplicationParticule = require("../data/inventory-data/indicators_application_particule.json");
const jsonDataApplicationClimate = require("../data/inventory-data/indicators_application_climate.json");
const jsonDataApplicationAcidification = require("../data/inventory-data/indicators_application_acidification.json");
const jsonDataApplicationResource = require("../data/inventory-data/indicators_application_resource.json");
const jsonDataApplicationWater = require("../data/inventory-data/indicators_application_water.json");
const jsonDataApplicationOzone = require("../data/inventory-data/indicators_application_ozone.json");
const jsonDataApplicationCancer = require("../data/inventory-data/indicators_application_cancer.json");
const jsonDataApplicationNoncancer = require("../data/inventory-data/indicators_application_noncancer.json");
const jsonDataApplicationPhotochemical = require("../data/inventory-data/indicators_application_photochemical.json");
const jsonDataApplicationTerrestrial = require("../data/inventory-data/indicators_application_terrestrial.json");
const jsonDataApplicationFreshwater = require("../data/inventory-data/indicators_application_freshwater.json");
const jsonDataApplicationMarine = require("../data/inventory-data/indicators_application_marine.json");
const jsonDataApplicationEcotoxicity = require("../data/inventory-data/indicators_application_ecotoxicity.json");
const jsonDataApplicationLand = require("../data/inventory-data/indicators_application_land.json");
const jsonDataApplicationFossils = require("../data/inventory-data/indicators_application_fossils.json");
/**
 * GET INVENTORIES INFORMATIONS
 */
router.get("", (req, res) => {
    if (!req.query.inventoryDate)
        res.send([
            {
                inventoryDate: "06-2023",
                creationDate: new Date("06 April 2023 14:48 UTC"),
                lastUpdateDate: new Date("07 April 2023 14:49 UTC"),
                organization: "SSG",
                dataCenterCount: 2,
                physicalEquipmentCount: 5,
                virtualEquipmentCount: 2,
                applicationCount: 14,
                integrationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("24 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:20 UTC"),
                        batchName: "Batch03",
                        resultFileUrl: "https://www.soprasteria.com/",
                        resultFileSize: 11255,
                    },
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("24 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:20 UTC"),
                        batchName: "Batch01",
                        resultFileUrl: "https://www.soprasteria.com/",
                        resultFileSize: 11255,
                    },
                ],
                evaluationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:12 UTC"),
                        batchName: "Batch01",
                        progressPercentage: "50%",
                    },
                ],
            },
            {
                inventoryDate: "04-2023",
                creationDate: new Date("12 April 2023 08:48 UTC"),
                lastUpdateDate: new Date("18 April 2023 14:55 UTC"),
                organization: "SSG",
                dataCenterCount: 4,
                physicalEquipmentCount: 17,
                virtualEquipmentCount: 21,
                applicationCount: 24,
                integrationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:20 UTC"),
                        batchName: "Batch02",
                        resultFileUrl: "https://www.soprasteria.com/",
                        resultFileSize: 11255,
                    },
                ],
                evaluationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:12 UTC"),
                        batchName: "Batch03",
                        progressPercentage: "50%",
                    },
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:12 UTC"),
                        batchName: "Batch02",
                        progressPercentage: "50%",
                    },
                ],
            },
        ]);
    else if (req.query.inventoryDate === "06-2023") {
        res.send([
            {
                inventoryDate: "06-2023",
                creationDate: new Date("06 April 2023 14:48 UTC"),
                lastUpdateDate: new Date("07 April 2023 14:49 UTC"),
                organization: "SSG",
                dataCenterCount: 2,
                physicalEquipmentCount: 1,
                virtualEquipmentCount: 2,
                applicationCount: 14,
                integrationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("24 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:20 UTC"),
                        batchName: "Batch03",
                        resultFileUrl: "https://www.soprasteria.com/",
                        resultFileSize: 11255,
                    },
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("24 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:20 UTC"),
                        batchName: "Batch01",
                        resultFileUrl: "https://www.soprasteria.com/",
                        resultFileSize: 11255,
                    },
                ],
                evaluationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:12 UTC"),
                        batchName: "Batch01",
                        progressPercentage: "50%",
                    },
                ],
            },
        ]);
    } else if (req.query.inventoryDate === "04-2023") {
        res.send([
            {
                inventoryDate: "04-2023",
                creationDate: new Date("12 April 2023 08:48 UTC"),
                lastUpdateDate: new Date("18 April 2023 14:55 UTC"),
                organization: "SSG",
                dataCenterCount: 4,
                physicalEquipmentCount: 17,
                virtualEquipmentCount: 21,
                applicationCount: 24,
                integrationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:20 UTC"),
                        batchName: "Batch02",
                        resultFileUrl: "https://www.soprasteria.com/",
                        resultFileSize: 11255,
                    },
                ],
                evaluationReports: [
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:12 UTC"),
                        batchName: "Batch03",
                        progressPercentage: "50%",
                    },
                    {
                        batchStatusCode: "COMPLETED",
                        createTime: new Date("28 April 2023 10:12 UTC"),
                        endTime: new Date("28 April 2023 13:12 UTC"),
                        batchName: "Batch02",
                        progressPercentage: "50%",
                    },
                ],
            },
        ]);
    } else {
        res.sendStatus(404);
    }
});

/**
 *  CREATE/DELETE AN INVENTORY
 */
router.post("/:inventoryDate", (req, res) => {
    res.status(200).send();
});

router.delete("/:inventoryDate", (req, res) => {
    res.status(200).send();
});

router.post("/:inventoryDate/loading", (reg, res) => {
    res.status(200).send();
});

router.post("/:inventoryDate/evaluation", (reg, res) => {
    res.status(200).send();
});

/**
 * FILES
 */
router.post("/:inventoryId/files", (req, res) => {
    res.status(200).send();
});

/**
 * EXPORT
 */
router.post("/:inventoryDate/export", (req, res) => {
    res.status(200).send();
});

/**
 * INDICATORS
 */
router.get("/:inventoryDate/indicators/equipments", (req, res) => {
    if (req.params.inventoryDate === "06-2023") {
        return res.send(jsonDataIndicator);
    }
    res.sendStatus(404);
});

router.get("/:inventoryDate/indicators/equipments/filters", (req, res) => {
    return res.send(jsonFilter);
});

router.delete("/:inventoryDate/indicators", (req, res) => {
    res.sendStatus(204);
});

router.get("/:inventoryDate/indicators/applications", (req, res) => {
    res.send(jsonDataApplicationIndicator);
});

router.get("/:inventoryDate/indicators/applications/filters", (req, res) => {
    return res.send(jsonFilterApp);
});

router.get("/:inventoryDate/indicators/applications/:app/:criteria", (req, res) => {
    if (req.params.criteria === "ionising-radiation") {
        return res.send(jsonDataApplicationRadiation);
    } else if (req.params.criteria === "climate-change") {
        return res.send(jsonDataApplicationClimate);
    } else if (req.params.criteria === "acidification") {
        return res.send(jsonDataApplicationAcidification);
    } else if (req.params.criteria === "particulate-matter") {
        return res.send(jsonDataApplicationParticule);
    } else if (req.params.criteria === "resource-use") {
        return res.send(jsonDataApplicationResource);
    } else if (req.params.criteria === "ozone-depletion") {
        return res.send(jsonDataApplicationOzone);
    } else if (req.params.criteria === "photochemical-ozone-formation") {
        return res.send(jsonDataApplicationPhotochemical);
    } else if (req.params.criteria === "eutrophication-terrestrial") {
        return res.send(jsonDataApplicationTerrestrial);
    } else if (req.params.criteria === "eutrophication-freshwater") {
        return res.send(jsonDataApplicationFreshwater);
    } else if (req.params.criteria === "eutrophication-marine") {
        return res.send(jsonDataApplicationMarine);
    } else if (req.params.criteria === "ecotoxicity-freshwater") {
        return res.send(jsonDataApplicationEcotoxicity);
    } else if (req.params.criteria === "resource-use-fossils") {
        return res.send(jsonDataApplicationFossils);
    } else {
        res.sendStatus(404);
    }
});

router.get("/:inventoryDate/indicators/datacenters", (req, res) => {
    if (req.params.inventoryDate === "06-2023") {
        return res.send(jsonDataCenter);
    }
    res.sendStatus(404);
});

router.get("/:inventoryDate/indicators/physicalEquipmentsAvgAge", (req, res) => {
    if (req.params.inventoryDate === "06-2023") {
        return res.send([
            {
                organisation: "SSG",
                inventoryDate: "06-2023",
                country: "France",
                type: "Monitor",
                nomEntite: null,
                statut: "Retired",
                poids: 50,
                ageMoyen: 1.5,
            },
            {
                organisation: "SSG",
                inventoryDate: "06-2023",
                country: "Spain",
                type: "Smartphone",
                nomEntite: "ACME FRANCE",
                statut: "Retired",
                poids: 70,
                ageMoyen: 1.8,
            },
            {
                organisation: "SSG",
                inventoryDate: "06-2023",
                country: "Germany",
                type: "Monitor",
                nomEntite: "ACME SERVICES",
                statut: "On order",
                poids: 200,
                ageMoyen: 1.3,
            },
        ]);
    }
    res.sendStatus(404);
});

router.get("/:inventoryDate/indicators/physicalEquipmentsLowImpact", (req, res) => {
    if (req.params.inventoryDate === "06-2023") {
        return res.send([
            {
                organisation: "SSG",
                inventoryDate: "06-2023",
                paysUtilisation: "France",
                type: "Monitor",
                nomEntite: null,
                statut: "Retired",
                quantite: 50,
                lowImpact: true,
            },
            {
                organisation: "SSG",
                inventoryDate: "06-2023",
                paysUtilisation: "Spain",
                type: "Smartphone",
                nomEntite: "ACME FRANCE",
                statut: "Retired",
                quantite: 70,
                lowImpact: false,
            },
            {
                organisation: "SSG",
                inventoryDate: "06-2023",
                paysUtilisation: "Germany",
                type: "Monitor",
                nomEntite: "ACME SERVICES",
                statut: "On order",
                quantite: 30,
                lowImpact: true,
            },
        ]);
    }
    res.sendStatus(404);
});

module.exports = router;
