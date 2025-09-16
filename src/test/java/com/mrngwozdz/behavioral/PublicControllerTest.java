package com.mrngwozdz.behavioral;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.AbstractIntegrationTest;
import com.mrngwozdz.api.model.response.ProcessMessageResponse;
import com.mrngwozdz.controller.PublicControllerUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PublicControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldHandleRegularMessage() {
        var request = """
                    {"text": "Hello World!"}
                    """;
        PublicControllerUtils.processMessage(request)
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);
    }

    @Test
    void shouldHandlePngUploadWithText() throws IOException {
        // Create a minimal PNG byte array (1x1 transparent PNG)
        byte[] originalPngBytes = {
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

        InputStream pngStream = new ByteArrayInputStream(originalPngBytes);

        var response = PublicControllerUtils.uploadFile(pngStream, "test.png", "Hello with image!")
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);

        // Parse the JSON response to get image URL
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.processMessage().value());

        assertThat(jsonNode.has("image_url")).isTrue();
        String imageUrl = jsonNode.get("image_url").asText();
        assertThat(imageUrl).contains("http").contains(".png");

        // Download the image from MinIO and verify it's the same
        URI uri = URI.create(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getContentType()).isEqualTo("image/png");

        try (InputStream downloadedImageStream = connection.getInputStream()) {
            byte[] downloadedBytes = downloadedImageStream.readAllBytes();
            assertThat(downloadedBytes).isEqualTo(originalPngBytes);
        }
    }

}