/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
const express = require("express");
const app = express();
const cors = require("cors");
const port = 8082;

const inventories = require("./routes/inventories");
const digitalServices = require("./routes/digital-services");
const users = require("./routes/users");
const version = require("./routes/version");
const randomResponseDelay = require("./random-delay-middleware");
const subscribers = require("./routes/subscribers");
const organizations = require("./routes/organization");

app.use(cors());

app.use(randomResponseDelay(100));

app.use(express.json());

/**
 * Inventories
 */
app.use("/subscribers/SOPRA-STERIA-GROUP/organizations/DEMO/inventories", inventories);

/**
 * Digital Services
 */
app.use(
    "/subscribers/SOPRA-STERIA-GROUP/organizations/DEMO/digital-services",
    digitalServices,
);

/**
 * Users
 */
app.use("/users", users);

app.use("/version", version);

// app.use(`/subscribers?user=`, subscribers);
app.use("/subscribers/SOPRA-STERIA-GROUP/organizations/DEMO/subscribers", subscribers);

app.use("/subscribers/SOPRA-STERIA-GROUP/organizations/DEMO/organizations/", organizations);

app.use("/subscribers/SOPRA-STERIA-GROUP/organizations/DEMO/subscribers/users",users)

app.use("/subscribers/SOPRA-STERIA-GROUP/organizations/DEMO/users", organizations);

app.listen(port, () => {
    console.log(`Mock server listening on port ${port}`);
});
