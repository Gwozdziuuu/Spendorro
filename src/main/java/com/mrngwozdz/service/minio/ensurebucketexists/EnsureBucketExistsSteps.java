package com.mrngwozdz.service.minio.ensurebucketexists;

import com.mrngwozdz.platform.result.Failure;
import io.minio.MinioClient;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import static com.mrngwozdz.service.minio.ensurebucketexists.EnsureBucketExistsMethods.*;

@Slf4j
public class EnsureBucketExistsSteps {

    private EnsureBucketExistsSteps() {}

    public static Either<Failure, EnsureBucketExistsHelper> checkIfBucketExist(EnsureBucketExistsHelper h, MinioClient minioClient, String bucketName) {
        return bucketExist(minioClient, bucketName).map(h::setBucketExists);
    }

    public static Either<Failure, EnsureBucketExistsHelper> createBucketIfNotExist(EnsureBucketExistsHelper h, MinioClient minioClient, String bucketName) {
        if (!h.isBucketExists()) {
            log.info("Creating bucket: {}", bucketName);
            return createBucket(minioClient, bucketName)
                    .flatMap(ignored -> setBucketPublic(minioClient, bucketName))
                    .map(ignored -> h);
        }
        return Either.right(h);
    }

}

