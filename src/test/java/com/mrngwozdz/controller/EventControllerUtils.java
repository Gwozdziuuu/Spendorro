package com.mrngwozdz.controller;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;

@Slf4j
public class EventControllerUtils {

    public static ValidatableResponse getEvents(int limit) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/events?limit=%d".formatted(limit))
                .then();
    }

    public static ValidatableResponse getEvents() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/events")
                .then();
    }

    public static ValidatableResponse getEventsByType(String eventType) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/events/type/%s".formatted(eventType))
                .then();
    }

}