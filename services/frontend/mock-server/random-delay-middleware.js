/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
function randomResponseDelay(minDelay) {
    minDelay = minDelay || 1000;
    return (req, res, next) => setTimeout(() => next(), randomDelay(minDelay))
}

function randomDelay(minDelay) {
    return minDelay + Math.floor(Math.random() * minDelay) + 1;
}

module.exports = randomResponseDelay;
