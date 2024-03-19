/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.utils;

import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

/**
 * Delimited Line Tokenizer without quote char
 */
public class QuotelessLineTokenizer extends DelimitedLineTokenizer {
    @Override
    protected boolean isQuoteCharacter(char c) {
        return false;
    }
}
