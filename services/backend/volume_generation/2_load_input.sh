#!/bin/bash

#
# G4IT
# Copyright 2023 Sopra Steria
#
# This product includes software developed by
# French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
#

DATE_LOT=$(date +'%Y-%m-%d')

. ./.env
. ./utils.sh

rm -f progress.log

#inventoryId=43

if [ "${inventoryId}" = "" ];then
  log "Create inventory simulation PERF_TEST"
  resp=$(curl -s -XPOST "$BACKEND_URL/subscribers/${SUBSCRIBER}/organizations/${ORGANIZATION_ID}/inventories" \
    -d"{\"name\":\"PERF_TEST\",\"type\":\"SIMULATION\",\"isNewArch\":true}" -H "Content-Type: application/json")

  echo $resp
  inventoryId=$(echo $resp | grep -o '"id":[^,]*' | grep -o '[^:]*$')
fi

log_n "Load Input with inventory id: ${inventoryId}- " | tee -a progress.log
curl -s -XPOST "$BACKEND_URL/subscribers/${SUBSCRIBER}/organizations/${ORGANIZATION_ID}/inventories/${inventoryId}/load-input-files" \
  -F DATACENTER=@data/1-datacenter.csv \
  -F EQUIPEMENT_PHYSIQUE=@data/2-physical-equipment.csv \
  -F EQUIPEMENT_VIRTUEL=@data/3-virtual-equipment.csv \
  -F APPLICATION=@data/4-application.csv | tee -a progress.log

echo "" | tee -a progress.log

sleep 2
