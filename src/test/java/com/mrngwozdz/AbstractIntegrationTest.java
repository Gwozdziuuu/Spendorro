package com.mrngwozdz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mrngwozdz.service.appevent.data.EventRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import io.quarkus.test.TestTransaction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MinIOContainer;

@Slf4j
@QuarkusTestResource(AbstractIntegrationTest.MinioTestResource.class)
public abstract class AbstractIntegrationTest {

    protected static MinIOContainer minioContainer = new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z")
            .withUserName("minioadmin")
            .withPassword("minioadmin");

    protected ObjectMapper objectMapper;

    @Inject
    protected EventRepository eventRepository;

    @BeforeEach
    public void setup() {
        RestAssured.basePath = "";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void cleanupBeforeTest() {
        log.info("Running cleanup!");
        eventRepository.deleteAllEvents();
    }

    public static class MinioTestResource implements io.quarkus.test.common.QuarkusTestResourceLifecycleManager {
        @Override
        public java.util.Map<String, String> start() {
            if (!minioContainer.isRunning()) {
                minioContainer.start();
            }
            return java.util.Map.of(
                    "minio.endpoint", minioContainer.getS3URL(),
                    "minio.access-key", minioContainer.getUserName(),
                    "minio.secret-key", minioContainer.getPassword()
            );
        }

        @Override
        public void stop() {
            // Don't stop the container here, let it run for the entire test suite
            // minioContainer.stop();
        }
    }
}