#
# G4IT
# Copyright 2023 Sopra Steria
#
# This product includes software developed by
# French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
#

function log() {
    echo "$(date +'%Y-%m-%d %H:%M:%S.%3N') - $@"
}

function log_n() {
    echo -n "$(date +'%Y-%m-%d %H:%M:%S.%3N') - $@"
}