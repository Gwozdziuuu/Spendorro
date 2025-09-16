package com.mrngwozdz.service.minio.uploadimage;

import lombok.Getter;

@Getter
public final class UploadImageHelper {
    private String objectName;
    private String fileName;
    private String contentType;

    public UploadImageHelper setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    public UploadImageHelper setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public UploadImageHelper setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}