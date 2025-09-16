package com.mrngwozdz.controller;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

import static io.restassured.RestAssured.given;

@Slf4j
public class PublicControllerUtils {

    public static ValidatableResponse processMessage(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/message")
                .then();
    }

    public static ValidatableResponse uploadFile(InputStream fileStream, String fileName, String text) {
        return given()
                .multiPart("file", fileName, fileStream, "image/png")
                .formParam("text", text)
                .formParam("fileName", fileName)
                .when()
                .post("/upload")
                .then();
    }

}