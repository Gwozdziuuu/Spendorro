package com.mrngwozdz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mrngwozdz.service.appevent.data.EventRepository;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import io.quarkus.test.TestTransaction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
public abstract class AbstractIntegrationTest {

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
}