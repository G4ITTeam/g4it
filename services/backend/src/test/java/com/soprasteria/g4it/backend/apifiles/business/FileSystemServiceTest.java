/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apifiles.business;

import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileSystemServiceTest {

    @InjectMocks
    FileSystemService fileSystemService;

    @Test
    void testCheckFiles() {

        // check empty and null
        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", List.of());
        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", (Object) null);

        List<MultipartFile> okFiles = List.of(new MockMultipartFile(
                "goodFile",
                "goodFile.txt",
                "text/csv",
                "goodFile!".getBytes()
        ));
        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", okFiles);
    }

    @Test
    void testCheckFiles_failWrongType() {
        List<MultipartFile> failPlainTextFiles = List.of(
                new MockMultipartFile(
                        "goodFile",
                        "goodFile.txt",
                        "text/csv",
                        "goodFile!".getBytes()
                ),
                new MockMultipartFile(
                        "badFile",
                        "badFile.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "badFile!".getBytes()
                ));
        Assertions.assertThrows(BadRequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", failPlainTextFiles);
        });
    }
}
