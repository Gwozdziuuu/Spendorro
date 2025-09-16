package com.mrngwozdz.service.minio.uploadimage;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
public class UploadImageMethods {

    private UploadImageMethods() {}

    public static Either<Failure, String> generateObjectName(String fileName) {
        try {
            String uuid = UUID.randomUUID().toString();
            String extension = getFileExtension(fileName);
            String objectName = String.format("images/%s%s", uuid, extension);
            return Either.right(objectName);
        } catch (Exception e) {
            log.error("Error generating object name: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Failed to generate object name: " + e.getMessage()));
        }
    }

    public static Either<Failure, Void> uploadObject(MinioClient minioClient, String bucketName,
                                                    String objectName, InputStream imageStream, String contentType) {
        return executeMinioOperation(() -> {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(imageStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build());
            return null;
        }, "uploading object", UploadImageMethods::mapErrorResponseException);
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
            case "InvalidObjectName" -> Failure.of(ErrorCode.VALIDATION, "Invalid object name: " + e.getMessage());
            default -> Failure.of(ErrorCode.IO_ERROR, "MinIO error: " + e.getMessage());
        };
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg"; // default
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}