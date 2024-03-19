/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Pipe, PipeTransform } from '@angular/core';
import { BatchStatusEnum } from '../utils/batch-status.enum';
import { TranslateService } from "@ngx-translate/core";

@Pipe({
    name: 'batchStatusRenderer'
})
export class BatchStatusRendererPipe implements PipeTransform {
    constructor(private translate: TranslateService) {}

    transform(value: number|string|undefined, ...args: any[]): any {
        switch (value) {
            case BatchStatusEnum.DATA_EXTRACTION:
                return this.translate.instant(`inventories.evaluation_batch_status.data_extraction`);                 
            case BatchStatusEnum.DATA_EXPOSITION_TO_NUMECOVAL:
                return this.translate.instant(`inventories.evaluation_batch_status.data_exposition`);  
            case BatchStatusEnum.CALCUL_SUBMISSION_TO_NUMECOVAL:
                return this.translate.instant(`inventories.evaluation_batch_status.calculation_submission`);  
            case BatchStatusEnum.CALCUL_IN_PROGRESS:
                return this.translate.instant(`inventories.evaluation_batch_status.calculation_in_progress`); 
        }
    }
}