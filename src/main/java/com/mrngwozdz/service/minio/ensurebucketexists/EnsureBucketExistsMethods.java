package com.mrngwozdz.service.minio.ensurebucketexists;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.*;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class EnsureBucketExistsMethods {

    private EnsureBucketExistsMethods() {}

    public static Either<Failure, Boolean> bucketExist(MinioClient minioClient, String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            return Either.right(exists);
        } catch (ServerException e) {
            log.error("MinIO server error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNAVAILABLE, "MinIO server error: " + e.getMessage()));
        } catch (InsufficientDataException e) {
            log.error("Insufficient data error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Insufficient data error: " + e.getMessage()));
        } catch (ErrorResponseException e) {
            log.error("MinIO error response while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(mapErrorResponseException(e));
        } catch (IOException e) {
            log.error("IO error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "IO error: " + e.getMessage()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Security/cryptographic error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Security error: " + e.getMessage()));
        } catch (InvalidResponseException | XmlParserException e) {
            log.error("Invalid response/parsing error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Invalid response error: " + e.getMessage()));
        } catch (InternalException e) {
            log.error("Internal MinIO error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNKNOWN, "Internal MinIO error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while checking bucket existence: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNKNOWN, "Unexpected error: " + e.getMessage()));
        }
    }

    public static Either<Failure, Void> createBucket(MinioClient minioClient, String bucketName) {
        return executeMinioOperation(() -> {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return null;
        }, "creating bucket", EnsureBucketExistsMethods::mapErrorResponseExceptionForCreate);
    }

    public static Either<Failure, Void> setBucketPublic(MinioClient minioClient, String bucketName) {
        String policy = String.format("""
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {"AWS": "*"},
                        "Action": "s3:GetObject",
                        "Resource": "arn:aws:s3:::%s/*"
                    }
                ]
            }
            """, bucketName);

        return executeMinioOperation(() -> {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build());
            log.info("Set bucket {} as public", bucketName);
            return null;
        }, "setting bucket policy", EnsureBucketExistsMethods::mapErrorResponseException);
    }

    private static <T> Either<Failure, T> executeMinioOperation(
            MinioOperation<T> operation,
            String operationName,
            java.util.function.Function<ErrorResponseException, Failure> errorMapper) {
        try {
            T result = operation.execute();
            return Either.right(result);
        } catch (ServerException e) {
            log.error("MinIO server error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNAVAILABLE, "MinIO server error: " + e.getMessage()));
        } catch (InsufficientDataException e) {
            log.error("Insufficient data error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Insufficient data error: " + e.getMessage()));
        } catch (ErrorResponseException e) {
            log.error("MinIO error response while {}: {}", operationName, e.getMessage(), e);
            return Either.left(errorMapper.apply(e));
        } catch (IOException e) {
            log.error("IO error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "IO error: " + e.getMessage()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Security/cryptographic error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Security error: " + e.getMessage()));
        } catch (InvalidResponseException | XmlParserException e) {
            log.error("Invalid response/parsing error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Invalid response error: " + e.getMessage()));
        } catch (InternalException e) {
            log.error("Internal MinIO error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNKNOWN, "Internal MinIO error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while {}: {}", operationName, e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNKNOWN, "Unexpected error: " + e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface MinioOperation<T> {
        T execute() throws Exception;
    }

    private static Failure mapErrorResponseException(ErrorResponseException e) {
        return switch (e.errorResponse().code()) {
            case "NoSuchBucket" -> Failure.of(ErrorCode.NOT_FOUND, "Bucket does not exist: " + e.getMessage());
            case "AccessDenied" -> Failure.of(ErrorCode.VALIDATION, "Access denied: " + e.getMessage());
            case "InvalidBucketName" -> Failure.of(ErrorCode.VALIDATION, "Invalid bucket name: " + e.getMessage());
            case "BucketAlreadyExists" -> Failure.of(ErrorCode.CONFLICT, "Bucket already exists: " + e.getMessage());
            default -> Failure.of(ErrorCode.IO_ERROR, "MinIO error: " + e.getMessage());
        };
    }

    private static Failure mapErrorResponseExceptionForCreate(ErrorResponseException e) {
        return switch (e.errorResponse().code()) {
            case "BucketAlreadyOwnedByYou", "BucketAlreadyExists" ->
                Failure.of(ErrorCode.CONFLICT, "Bucket already exists: " + e.getMessage());
            case "InvalidBucketName" ->
                Failure.of(ErrorCode.VALIDATION, "Invalid bucket name: " + e.getMessage());
            case "AccessDenied" ->
                Failure.of(ErrorCode.VALIDATION, "Access denied: " + e.getMessage());
            default ->
                Failure.of(ErrorCode.IO_ERROR, "MinIO error: " + e.getMessage());
        };
    }

}
