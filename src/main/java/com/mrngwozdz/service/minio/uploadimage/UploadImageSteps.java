package com.mrngwozdz.service.minio.uploadimage;

import com.mrngwozdz.platform.result.Failure;
import io.minio.MinioClient;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

import static com.mrngwozdz.service.minio.uploadimage.UploadImageMethods.*;

@Slf4j
public class UploadImageSteps {

    private UploadImageSteps() {}

    public static Either<Failure, UploadImageHelper> prepareObjectName(UploadImageHelper h, String fileName) {
        return generateObjectName(fileName).map(h::setObjectName);
    }

    public static Either<Failure, UploadImageHelper> uploadImageToMinio(UploadImageHelper h, MinioClient minioClient,
                                                                        String bucketName, InputStream imageStream) {
        log.info("Uploading image to MinIO: bucket={}, object={}", bucketName, h.getObjectName());
        return uploadObject(minioClient, bucketName, h.getObjectName(), imageStream, h.getContentType())
                .map(ignored -> h);
    }
}