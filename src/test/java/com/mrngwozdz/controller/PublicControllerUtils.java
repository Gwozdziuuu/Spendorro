package com.mrngwozdz.controller;

import com.mrngwozdz.api.model.request.MessageRequest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;

@Slf4j
public class PublicControllerUtils {

    public static ValidatableResponse processMessage(String text) {
        var request = new MessageRequest();
        request.setText(text);

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/message")
                .then();
    }

    public static ValidatableResponse processMessageWithInvalidData() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"invalid\": \"data\"}")
                .when()
                .post("/api/message")
                .then();
    }

    public static ValidatableResponse processEmptyMessage() {
        return processMessage("");
    }

    public static ValidatableResponse processNullMessage() {
        return processMessage(null);
    }
}