#!/bin/bash
#
# G4IT
# Copyright 2023 Sopra Steria
#
# This product includes software developed by
# French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
#

# Call: sh 1_generate_dataset.sh
NB_PH_EQ=${1:-10}
NB_VM_PER_SRV=${2:-2}
NB_APP_PER_VM=${3:-1}
NB_CLOUD_EQ=${1:-10}
HAS_ONE_ENTITY_BY_PH_EQ=${4:-false}

. ./.env
. ./utils.sh


set -f
ARR_VM_TYPE_EQV=(${VM_TYPE_EQV//,/ })
ARR_APP_TYPE_ENV=(${APP_TYPE_ENV//,/ })

if [ -d data ]; then
    rm -rf data
fi
mkdir data

# *** GENERATE DATASET ***
log "Generate input data in the 'data' local folder"
log "NB_PH_EQ=$NB_PH_EQ, NB_VM_PER_SRV=$NB_VM_PER_SRV, NB_APP_PER_VM=$NB_APP_PER_VM"

# *** 1-datacenter.csv ***
log "Generate input data : 1-datacenter.csv"
cp -f input_template/1-datacenter.csv data/

# *** 2-physical-equipment.csv ***
log "Generate input data : 2-physical-equipment.csv"

head -n1 input_template/2-physical-equipment.csv >data/2-physical-equipment.csv
PH_EQ_LINE=$(tail -n1 input_template/2-physical-equipment.csv)

for ((i = 1; i <= $NB_PH_EQ; i++)); do
  PH1=${PH_EQ_LINE//physical-eq-srv-/physical-eq-srv-$i}
  if [ "$HAS_ONE_ENTITY_BY_PH_EQ" = "true" ];then
    echo ${PH1//entity-/entity-$i}
  else
    echo ${PH1//entity-/entity-1}
  fi

done >> data/2-physical-equipment.csv

# *** 3-virtual-equipment.csv ***
log "Generate input data : 3-virtual-equipment.csv"
head -n1 input_template/3-virtual-equipment.csv >data/3-virtual-equipment.csv

VM_LINE=$(tail -n1 input_template/3-virtual-equipment.csv)

n=0

for ((i = 1; i <= $NB_PH_EQ; i++)); do
  for ((vm = 1; vm <= $NB_VM_PER_SRV; vm++)); do
      type_eqv=${ARR_VM_TYPE_EQV[$((n % ${#ARR_VM_TYPE_EQV[@]}))]}
      VM1=${VM_LINE//virtual-eq-/virtual-eq-$i.$vm}
      VM2=${VM1//physical-eq-srv-/physical-eq-srv-$i}
      VM3=${VM2//##VCPU##/$VM_VCPU}
      echo ${VM3//##TYPE_EQV##/${type_eqv}}
      n=$((n + 1))
    done
done >> data/3-virtual-equipment.csv

# *** Application.csv ***
log "Generate input data : 4-application.csv"
head -n1 input_template/4-application.csv >data/4-application.csv

APP_LINE=$(tail -n1 input_template/4-application.csv)

n=0
for ((i = 1; i <= $NB_PH_EQ; i++)); do
  for ((vm = 1; vm <= $NB_VM_PER_SRV; vm++)); do
      for ((app = 1; app <= $NB_APP_PER_VM; app++)); do
         type_env=${ARR_APP_TYPE_ENV[$((n % ${#ARR_APP_TYPE_ENV[@]}))]}
         APP1=${APP_LINE//application-/application-$app}
         APP2=${APP1//virtual-eq-/virtual-eq-$i.$vm}
         APP3=${APP2//physical-eq-srv-/physical-eq-srv-$i}
         echo ${APP3//##TYPE_ENV##/${type_env}}
         n=$((n + 1))
      done
    done
done >> data/4-application.csv

# *** 5-cloud-virtual-equipment.csv ***
log "Generate input data : 5-cloud-virtual-equipment.csv"

head -n1 input_template/5-cloud-virtual-equipment.csv >data/5-cloud-virtual-equipment.csv
CLOUD_EQ_LINE=$(tail -n1 input_template/5-cloud-virtual-equipment.csv)

for ((i = 1; i <= $NB_CLOUD_EQ; i++)); do
  echo ${CLOUD_EQ_LINE//cloud-/cloud-$i}
done >> data/5-cloud-virtual-equipment.csv

log "Generate input data : End"

echo "File,Size(MB),lines" >data/sizes.csv
total=0
lines=0
for file in $(ls data/ | grep -v sizes); do
    size=$(ls -l data/$file | cut -d' ' -f5)
    size=$(awk "BEGIN {printf \"%.3f\",$size/(1024 * 1024)}")
    line=$(wc -l data/$file | cut -d' ' -f1)
    echo "$file,$size,$line" >>data/sizes.csv
    total=$(awk "BEGIN {printf \"%.3f\",$total + $size}")
    lines=$((lines + $line))
done

echo "TOTAL,$total,$lines" >>data/sizes.csv

log "Report sizes generated in data/sizes.csv"
