/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.business;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class InputStreamMultipartFile implements MultipartFile {
    private final byte[] bytes;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public InputStreamMultipartFile(InputStream inputStream, String name, String originalFilename, String contentType) throws IOException {
        this.bytes = inputStream.readAllBytes();
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return bytes;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream output = new FileOutputStream(dest)) {
            output.write(bytes);
        }
    }
}