package com.mrngwozdz.integration.telegram;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.telegram.TelegramService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.control.Either;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class TelegramTest {

    @InjectMock
    TelegramService telegramService;

    @Test
    void shouldReturn200WhenTelegramServiceSucceeds() {
        // given
        when(telegramService.sendMessage(anyString(), anyString()))
                .thenReturn(Either.right(Success.of("Message sent successfully")));

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "chatId": "123456789",
                        "text": "Test message"
                    }
                    """)
        .when()
                .post("/telegram/send-message")
        .then()
                .statusCode(200)
                .body(equalTo("Message sent successfully"));
    }

    @Test
    void shouldReturn500WhenTelegramServiceReturnsIOError() {
        // given
        Failure failure = Failure.of(ErrorCode.IO_ERROR, "Failed to send message")
                .with("status", 400);
        when(telegramService.sendMessage(anyString(), anyString()))
                .thenReturn(Either.left(failure));

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "chatId": "123456789",
                        "text": "Test message"
                    }
                    """)
        .when()
                .post("/telegram/send-message")
        .then()
                .statusCode(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body("title", equalTo("IO_ERROR"))
                .body("detail", equalTo("Failed to send message"))
                .body("extensions.status", equalTo(400));
    }

    @Test
    void shouldReturn500WhenTelegramServiceReturnsUnknownError() {
        // given
        Failure failure = Failure.of(ErrorCode.UNKNOWN, "Error sending message")
                .with("exception", "Connection timeout");
        when(telegramService.sendMessage(anyString(), anyString()))
                .thenReturn(Either.left(failure));

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "chatId": "123456789",
                        "text": "Test message"
                    }
                    """)
        .when()
                .post("/telegram/send-message")
        .then()
                .statusCode(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body("title", equalTo("UNKNOWN"))
                .body("detail", equalTo("Error sending message"))
                .body("extensions.exception", equalTo("Connection timeout"));
    }

    @Test
    void shouldReturn400ForValidationError() {
        // given
        Failure failure = Failure.of(ErrorCode.VALIDATION, "Invalid chat ID");
        when(telegramService.sendMessage(anyString(), anyString()))
                .thenReturn(Either.left(failure));

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "chatId": "123456789",
                        "text": "Test message"
                    }
                    """)
        .when()
                .post("/telegram/send-message")
        .then()
                .statusCode(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body("title", equalTo("VALIDATION"))
                .body("detail", equalTo("Invalid chat ID"));
    }
}