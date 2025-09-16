package com.mrngwozdz.integration.minio;

import com.mrngwozdz.AbstractIntegrationTest;
import com.mrngwozdz.configuration.properties.MinioProperties;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.service.minio.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MinioServiceTest extends AbstractIntegrationTest {

    @Inject
    MinioService minioService;

    @Inject
    MinioProperties minioProperties;

    private MinioClient testMinioClient;
    private byte[] testPngBytes;

    @BeforeEach
    void setUp() {
        // Create test MinIO client to verify operations
        testMinioClient = MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();

        // Create a minimal PNG byte array (1x1 transparent PNG)
        testPngBytes = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
            0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
            0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00,
            0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42,
            0x60, (byte) 0x82
        };
    }

    @Test
    void shouldEnsureBucketExistsAfterServiceInitialization() throws Exception {
        // Given: MinioService is injected and initialized
        // When: we check if bucket exists using raw MinIO client
        boolean bucketExists = testMinioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.bucketName())
                .build());

        // Then: bucket should exist and be public
        assertThat(bucketExists).isTrue();
    }

    @Test
    void shouldUploadImageSuccessfully() throws IOException {
        // Given
        InputStream imageStream = new ByteArrayInputStream(testPngBytes);
        String fileName = "test-image.png";
        String contentType = "image/png";

        // When
        Either<Failure, String> result = minioService.uploadImage(imageStream, fileName, contentType);

        // Then
        assertThat(result.isRight()).isTrue();
        String objectName = result.get();
        assertThat(objectName).contains("images/").endsWith(".png");

        // Verify image was uploaded to MinIO
        verifyImageInMinIO(objectName);
    }

    @Test
    void shouldGenerateCorrectPublicUrl() {
        // Given
        String objectName = "images/test-object.png";

        // When
        String publicUrl = minioService.getPublicUrl(objectName);

        // Then
        assertThat(publicUrl).isEqualTo(String.format("%s/%s/%s",
                minioProperties.endpoint(),
                minioProperties.bucketName(),
                objectName));
    }

    @Test
    void shouldGeneratePresignedUrl() {
        // Given
        String objectName = "images/test-object.png";

        // When
        String presignedUrl = minioService.getShortPresignedUrl(objectName);

        // Then
        assertThat(presignedUrl).contains(minioProperties.endpoint());
        assertThat(presignedUrl).contains(minioProperties.bucketName());
        assertThat(presignedUrl).contains(objectName);
    }

    @Test
    void shouldUploadAndRetrieveImageContent() throws Exception {
        // Given
        InputStream imageStream = new ByteArrayInputStream(testPngBytes);
        String fileName = "test-verification.png";
        String contentType = "image/png";

        // When
        Either<Failure, String> uploadResult = minioService.uploadImage(imageStream, fileName, contentType);

        // Then
        assertThat(uploadResult.isRight()).isTrue();
        String objectName = uploadResult.get();

        // Verify content is the same
        try (InputStream downloadedStream = testMinioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.bucketName())
                .object(objectName)
                .build())) {
            byte[] downloadedBytes = downloadedStream.readAllBytes();
            assertThat(downloadedBytes).isEqualTo(testPngBytes);
        }
    }

    private void verifyImageInMinIO(String objectName) throws IOException {
        try (InputStream retrievedStream = testMinioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.bucketName())
                .object(objectName)
                .build())) {

            assertThat(retrievedStream).isNotNull();
            byte[] retrievedBytes = retrievedStream.readAllBytes();
            assertThat(retrievedBytes).isEqualTo(testPngBytes);
        } catch (Exception e) {
            throw new IOException("Failed to retrieve image from MinIO", e);
        }
    }
}