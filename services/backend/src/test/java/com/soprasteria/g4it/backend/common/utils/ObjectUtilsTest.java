/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Terminal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ObjectUtilsTest {

    @Test
    void getCsvStringShouldReturnValue() {
        Terminal terminal = Terminal.builder()
                .country("France")
                .build();

        Assertions.assertThat(ObjectUtils.getCsvString("country", terminal, Terminal.class)).isEqualTo("France");
        Assertions.assertThat(ObjectUtils.getCsvString("lifespan", terminal, Terminal.class)).isEqualTo("");
        Assertions.assertThat(ObjectUtils.getCsvString("lifespan", terminal, Terminal.class, null)).isEqualTo(null);
        Assertions.assertThat(ObjectUtils.getCsvString("unknown", terminal, Terminal.class)).isEqualTo("! Cannot read value by calling isUnknown !");
    }
}
