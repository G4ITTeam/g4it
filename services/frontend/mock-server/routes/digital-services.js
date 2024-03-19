/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
const express = require("express");
const router = express.Router();
const impactTerminals = require("../data/digital-service-data/digital_service_terminals_footprint.json");
const impactNetworks = require("../data/digital-service-data/digital_service_networks_footprint.json");
const impactServers = require("../data/digital-service-data/digital_service_servers_footprint.json");
const indicators = require("../data/digital-service-data/digital_service_indicators_footprint.json");
const referential = require("../data/digital-service-data/digital_service_referential.json");

const digitalServices = {};
let digitaServiceId = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6";
let digitalServiceCounter = 0;

/**
 * Creates a new digital service with minimum infos
 * @returns a freshly generated service
 */
function newDigitalService() {
    digitalServiceCounter++;
    return {
        uid: digitaServiceId + digitalServiceCounter,
        name: "Digital Service #" + digitalServiceCounter,
        creationDate: Date.now(),
        lastUpdateDate: Date.now(),
        lastCalculationDate: null,
        networks: [],
        servers: [],
        terminals: [],
    };
}

// init with first digital service
const tmp = newDigitalService();
digitalServices[tmp.uid] = tmp;

/**
 * List digital services
 */
router.get("", (req, res) => {
    res.send(Object.values(digitalServices));
});

/**
 * Create new digital service
 */
router.post("", (req, res) => {
    const digitalService = newDigitalService();
    digitalServices[digitalService.uid] = digitalService;
    res.send(digitalService);
});

/**
 * List referential of country
 */
router.get("/country", (req, res) => {
    res.send([
        "France",
        "Germany",
        "China",
        "Italy",
        "United States of Amrica",
    ]);
});

/**
 * List referential of devices
 */
router.get("/device-type", (req, res) => {
    res.send(referential.terminalsReferantials);
});

/**
 * List referential of network
 */
router.get("/network-type", (req, res) => {
    res.send(referential.networkReferentials);
});

/**
 * List host referential of server
 */
router.get("/server-host", (req, res) => {
    const type = req.query.type;
    if (type == "Compute") {
        res.send(referential.serverReferentialsCompute);
    } else if (type == "Storage") {
        res.send(referential.serverReferentialsStorage);
    }
});

/**
 * Update digital service
 */
router.put("/:uid", (req, res) => {
    const id = req.params.uid;
    const existingDigitalService = digitalServices[id];
    if (!existingDigitalService) return res.sendStatus(404);

    // update the digital service with request body
    digitalServices[id] = {
        ...existingDigitalService,
        ...req.body,
    };
    res.send(digitalServices[id]);
});

/**
 * Get one digital Service detail
 */
router.get("/:uid", (req, res) => {
    const id = req.params.uid;
    const existing = digitalServices[id];
    if (!existing) return res.sendStatus(404);

    // return detail of digital service
    res.send({
        ...existing,
        servers: [
            {
                uid: "lm0b2e0c-157c-4eb2-bb38-d81cer720e1c4",
                name: "Server A",
                mutualizationType: "Dedicated",
                type: "Storage",
                quantity: 3,
                host: {
                    code: "storage-bay--3",
                    value: "Server Storage M",
                },
                datacenter: {
                    uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                    name: "Default DC",
                    location: "France",
                    pue: 1,
                },
                totalDisk: 100,
                lifespan: 10.5,
                annualElectricConsumption: 1000,
                annualOperatingTime: 8760,
                vm: [],
            },
            {
                uid: "fdhyrb-4648gd-gdhfjri6-648hfr543",
                name: "Server abracadabra",
                mutualizationType: "Shared",
                type: "Compute",
                quantity: 3,
                host: {
                    code: "rack-server--30",
                    value: "Server Compute M",
                },
                datacenter: {
                    uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                    name: "Default DC",
                    location: "France",
                    pue: 1,
                },
                totalVCpu: 100,
                lifespan: 10.5,
                annualElectricConsumption: 1000,
                annualOperatingTime: 8760,
                vm: [
                    {
                        uid: "mm0b2e0c-157c-4eb2-bb38-d81cer720e1b5",
                        name: "Mon PC",
                        vCpu: 16,
                        quantity: 1,
                        annualOperatingTime: 8760,
                    },
                ],
            },
        ],
        terminals: [
            {
                uid: "ba0b2e0c-157c-4eb2-bb38-d81ce700e1c4",
                type: {
                    code: "smartphone-2",
                    value: "Mobile Phone",
                },
                country: "France",
                numberOfUsers: 15,
                yearlyUsageTimePerUser: 4.5,
            },
            {
                uid: "9ad33e32-40c3-11ee-be56-0242ac120002",
                type: {
                    code: "smartphone-2",
                    value: "Mobile Phone",
                },
                country: "France",
                numberOfUsers: 2,
                yearlyUsageTimePerUser: 2,
            },
            {
                uid: "a742127e-40c3-11ee-be56-0242ac120002",
                type: {
                    code: "set-top-box-1",
                    value: "Screen",
                },
                country: "Germany",
                numberOfUsers: 15,
                yearlyUsageTimePerUser: 8,
            },
            {
                uid: "ab998302-40c3-11ee-be56-0242ac120002",
                type: {
                    code: "portable-console-1",
                    value: "Portable console",
                },
                country: "Italy",
                numberOfUsers: 3,
                yearlyUsageTimePerUser: 4.5,
            },
        ],
        networks: [
            {
                uid: "c161bc63-49ea-4fc8-ae7e-0a49cf647c49",
                type: {
                    code: "fixe-line-network-1",
                    value: "Fixed FR",
                },
                yearlyQuantityOfGbExchanged: 75,
            },
            {
                uid: "f8d6764e-27a4-4468-91ac-dbf3b84d0496",
                type: {
                    code: "mobile-line-network-2",
                    value: "Mobile EU",
                },
                yearlyQuantityOfGbExchanged: 45,
            },
        ],
    });
});

/**
 * Delete a digital service
 */
router.delete("/:uid", (req, res) => {
    const id = req.params.uid;
    const existingDigitalService = digitalServices[id];
    if (!existingDigitalService) return res.sendStatus(404);

    delete digitalServices[id];
    res.status(200).send();
});

/**
 * launch an evaluation
 */
router.post("/:uid/evaluation", (req, res) => {
    const id = req.params.uid;
    const existingDigitalService = digitalServices[id];
    if (!existingDigitalService) return res.sendStatus(404);
    res.status(200).send();
});

/**
 * List datacenter referential of server
 */
router.get("/:uid/datacenters", (req, res) => {
    const id = req.params.uid;
    const existingDigitalService = digitalServices[id];
    if (!existingDigitalService) return res.sendStatus(404);
    res.send([
        {
            uid: "",
            name: "Default DC",
            location: "France",
            pue: 1.5,
        },
    ]);
});

/**
 * Get a digital service footprint
 */
router.get("/:uid/indicators", (req, res) => {
    const id = req.params.uid;
    const existing = digitalServices[id];
    if (!existing) return res.sendStatus(404);

    res.send(indicators);
});

/**
 * Get a digital service footprint
 */
router.get("/:uid/servers/indicators", (req, res) => {
    const id = req.params.uid;
    const existing = digitalServices[id];
    if (!existing) return res.sendStatus(404);

    res.send(impactServers);
});

/**
 * Get a digital service footprint
 */
router.get("/:uid/networks/indicators", (req, res) => {
    const id = req.params.uid;
    const existing = digitalServices[id];
    if (!existing) return res.sendStatus(404);

    res.send(impactNetworks);
});

/**
 * Get a digital service footprint
 */
router.get("/:uid/terminals/indicators", (req, res) => {
    const id = req.params.uid;
    const existing = digitalServices[id];
    if (!existing) return res.sendStatus(404);

    res.send(impactTerminals);
});

module.exports = router;
