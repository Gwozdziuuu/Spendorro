package com.mrngwozdz.service.minio;

import com.mrngwozdz.configuration.properties.MinioProperties;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.minio.ensurebucketexists.EnsureBucketExistsHelper;
import com.mrngwozdz.service.minio.uploadimage.UploadImageHelper;
import com.mrngwozdz.service.minio.uploadimage.UploadImageSteps;
import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

import static com.mrngwozdz.service.minio.ensurebucketexists.EnsureBucketExistsSteps.checkIfBucketExist;
import static com.mrngwozdz.service.minio.ensurebucketexists.EnsureBucketExistsSteps.createBucketIfNotExist;

@Slf4j
@ApplicationScoped
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String endpoint;

    @Inject
    public MinioService(MinioProperties minioProperties) {
        this.bucketName = minioProperties.bucketName();
        this.endpoint = minioProperties.endpoint();
        this.minioClient = MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();

        ensureBucketExists().fold(
            failure -> {
                log.error("Failed to initialize MinIO bucket: {}", failure.message());
                throw new RuntimeException("Failed to initialize MinIO: " + failure.message());
            },
            success -> {
                log.info("MinIO bucket initialization completed successfully");
                return null;
            }
        );
    }

    private Either<Failure, Success<Void>> ensureBucketExists() {
        EnsureBucketExistsHelper helper = new EnsureBucketExistsHelper();
        return checkIfBucketExist(helper, minioClient, bucketName)
                .flatMap(h -> createBucketIfNotExist(h, minioClient, bucketName))
                .map(ignored -> Success.of(null));
    }

    public Either<Failure, String> uploadImage(InputStream imageStream, String fileName, String contentType) {
        return uploadImageInternal(imageStream, fileName, contentType);
    }

    private Either<Failure, String> uploadImageInternal(InputStream imageStream, String fileName, String contentType) {
        UploadImageHelper helper = new UploadImageHelper()
                .setFileName(fileName)
                .setContentType(contentType);

        return UploadImageSteps.prepareObjectName(helper, fileName)
                .flatMap(h -> UploadImageSteps.uploadImageToMinio(h, minioClient, bucketName, imageStream))
                .map(UploadImageHelper::getObjectName);
    }

    public String getShortPresignedUrl(String objectName) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(10 * 60) // URL valid for 10 minutes
                    .build());

            log.info("Generated short presigned URL for object: {}", objectName);
            return url;
        } catch (Exception e) {
            log.error("Failed to generate short presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate short presigned URL", e);
        }
    }

    public String getPublicUrl(String objectName) {
        String url = String.format("%s/%s/%s", endpoint, bucketName, objectName);
        log.info("Generated public URL for object: {}", objectName);
        return url;
    }

}