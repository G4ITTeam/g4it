/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
const express = require("express");
const router = express.Router();

router.get("/me", (req, res) => {
    res.send({
        email: "prenom.nom@soprasteria.com",
        firstName: "prenom",
        lastName: "nom",
        userId: 1,
        subscribers: [
            {
                name: "SOPRA-STERIA-GROUP",
                defaultFlag: true,
                organizations: [
                    {
                        name: "DEMO",
                        defaultFlag: false,
                        roles: [
                            "ROLE_DIGITAL_SERVICE_READ",
                            "ROLE_DIGITAL_SERVICE_WRITE",
                        ],
                    },
                ],
            },
            {
                name: "PasSSG",
                defaultFlag: false,
                organizations: [
                    {
                        name: "123",
                        defaultFlag: true,
                        roles: [
                            "ROLE_INVENTORY_READ",
                            "ROLE_INVENTORY_WRITE",
                            "ROLE_DIGITAL_SERVICE_WRITE",
                            "ROLE_DIGITAL_SERVICE_READ",
                            "ROLE_SUBSCRIBER_ADMINISTRATOR",
                        ],
                    },
                ],
            },
        ],
    });
});

router.get("", (req, res) => {
    res.send({
        users: [
            {
                id: 0,
                firstName: "Denis",
                lastName: "Lemercier",
                email: "denis.lemercier@mail.com",
                role: ["DIGITAL_SERVICE_READ", "DIGITAL_SERVICE_WRITE"],
            },
            {
                id: 2,
                firstName: "Prenom",
                lastName: "Nom",
                email: "prenom.nom@mail2.com",
                role: [],
            },
        ],
    });
});

router.post("", (req, res) => {
    res.status(200).send();
    // res.status(400).send("You can't add roles and permissions to this user for now. Please try again later or contact your administrator.");
});

module.exports = router;
