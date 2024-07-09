/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
export interface FileSystemInputFile {
    inventoryId?: number;
    type: string;
    metadata: Metadata;
    position: number;
}

export interface Metadata {
    creationTime?: string;
    size?: string;
}

export interface FileSystemUploadFile {
    inventoryId?: number;
    type: string;
    files: FormData;
}

export interface FileType {
    value: string;
    text: string;
}

export interface LoadingBody {
    files: FileDescription[];
}

export interface FileDescription {
    name: string;
    type: string;
    metadata: Metadata;
}

export interface TemplateFileDescription {
    name: string;
    type: string;
    metadata: Metadata;
    displayFileName?: string;
    csvFileType?: string;
}
