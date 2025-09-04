package com.mrngwozdz.behavioral;

import com.mrngwozdz.AbstractIntegrationTest;
import com.mrngwozdz.api.model.response.ProcessMessageResponse;
import com.mrngwozdz.controller.PublicControllerUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
class PublicControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldHandleRegularMessage() {
        PublicControllerUtils.processMessage("Hello")
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);
    }

    @Test
    void shouldHandleSpecialCharactersInMessage() {
        PublicControllerUtils.processMessage("Café & Restaurant - €15.50!")
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);
    }

    @Test
    void shouldHandleImageUpload() {
        byte[] testImageData = {
            (byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n',
            0x00, 0x00, 0x00, 0x0D,
            'I', 'H', 'D', 'R',
            0x00, 0x00, 0x00, 0x01,
            0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00
        };

        given()
                .multiPart("file", "test-image.png", testImageData, "image/png")
                .multiPart("text", "Test receipt image")
                .when()
                .post("/api/upload")
                .then()
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);
    }

    @Test
    void shouldHandleImageUploadWithoutText() {
        byte[] testImageData = {
            (byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n',
            0x00, 0x00, 0x00, 0x0D,
            'I', 'H', 'D', 'R',
            0x00, 0x00, 0x00, 0x01,
            0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00
        };

        given()
                .multiPart("file", "receipt.png", testImageData, "image/png")
                .when()
                .post("/api/upload")
                .then()
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);
    }

    @Test
    void shouldRejectInvalidFileType() {
        byte[] testFileData = {
            'P', 'D', 'F', '-', '1', '.', '4', '\n',
            '1', '0', ' ', '0', ' ', 'o', 'b', 'j'
        };

        var response = given()
                .multiPart("file", "document.pdf", testFileData, "application/pdf")
                .multiPart("fileName", "document.pdf")
                .when()
                .post("/api/upload")
                .then()
                .statusCode(400)
                .extract()
                .jsonPath();
        assertThat(response.getString("code")).isEqualTo("INVALID_FILE_TYPE");
    }

    @Test
    void shouldRejectTxtFile() {
        byte[] testFileData = "This is a text file content".getBytes();

        var response = given()
                .multiPart("file", "document.txt", testFileData, "text/plain")
                .multiPart("fileName", "document.txt")
                .when()
                .post("/api/upload")
                .then()
                .statusCode(400)
                .extract()
                .jsonPath();
        assertThat(response.getString("code")).isEqualTo("INVALID_FILE_TYPE");
    }

}