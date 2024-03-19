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
        username: "prenom.nom@soprasteria.com",
        subscribers: [
            {
                name: "SSG",
                defaultFlag: true,
                organizations: [
                    {
                        name: "SSG",
                        defaultFlag: false,
                        roles: ["ROLE_DIGITAL_SERVICE"],
                    },
                    {
                        name: "G4IT",
                        defaultFlag: true,
                        roles: ["ROLE_INVENTORY", "ROLE_DIGITAL_SERVICE"],
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
                        roles: ["ROLE_DIGITAL_SERVICE"],
                    },
                    {
                        name: "456",
                        defaultFlag: false,
                        roles: ["ROLE_INVENTORY"],
                    },
                ],
            },
        ],
    });
});

module.exports = router;
