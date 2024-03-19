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
    res.send({
        subscribers: [
            {
                numEcoEval: null,
                g4it: "1.0.0",
            },
        ],
    });
});

module.exports = router;
