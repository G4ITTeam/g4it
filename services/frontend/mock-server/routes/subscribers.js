/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
const express = require("express");
const router = express.Router();

router.get("", (req, res) => {
    let user = req.query.user;
    res.send({
        subscribers: [
            {
                name: "SOPRA-STERIA-GROUP",
                organizations: [
                    {
                        id: 1,
                        name: "DEMO",
                        statusCode: "ACTIVE",
                        deletionDate: "2024-04-29T15:05:37.654511Z",
                        dataRetentionDay: null,
                    },

                    {
                        id: 2,
                        name: "DEMO-2",
                        statusCode: "ACTIVE",
                        deletionDate: "2024-04-29T15:05:37.654511Z",
                        dataRetentionDay: null,
                    },
                ],
            },
            {
                name: "ADEO",
                organizations: [
                    {
                        id: 3,
                        name: "DEMO",
                        statusCode: "TO_BE_DELETED",
                        deletionDate: "2024-04-29T15:05:37.654511Z",
                        dataRetentionDay: 7,
                    },

                    {
                        id: 4,
                        name: "MAIN",
                        statusCode: "TO_BE_DELETED",
                        deletionDate: "2024-04-29T15:05:37.654511Z",
                        dataRetentionDay: 7,
                    },
                ],
            },
        ],
    });
});

module.exports = router;
