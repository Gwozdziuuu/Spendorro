package com.mrngwozdz.behavioral;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.AbstractIntegrationTest;
import com.mrngwozdz.api.model.response.ProcessMessageResponse;
import com.mrngwozdz.controller.PublicControllerUtils;
import com.mrngwozdz.integration.openai.OpenAiService;
import com.mrngwozdz.integration.openai.model.OpenAiProcessRequest;
import com.mrngwozdz.platform.result.Failure;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PublicControllerTest extends AbstractIntegrationTest {

    @InjectMock
    OpenAiService openAiService;

    @Test
    void shouldHandleRegularMessage() {
        // Mock OpenAI service response
        Mockito.when(openAiService.processRequest(Mockito.any(OpenAiProcessRequest.class)))
                .thenReturn(Either.right("{\"result\": \"Hello response from OpenAI!\"}"));

        var request = """
                    {"text": "Hello World!"}
                    """;
        var response = PublicControllerUtils.processMessage(request)
                .statusCode(200)
                .extract().as(ProcessMessageResponse.class);

        // Verify OpenAI service was called
        Mockito.verify(openAiService).processRequest(Mockito.any(OpenAiProcessRequest.class));

        // Verify the response contains OpenAI result
        assertThat(response.processMessage().value()).contains("Hello response from OpenAI!");
    }

    @Test
    void shouldHandlePngUploadWithText() throws IOException {
        // Mock OpenAI service response for file upload
        Mockito.when(openAiService.processRequest(Mockito.any(OpenAiProcessRequest.class)))
                .thenReturn(Either.right("{\"result\": \"Image processed successfully!\"}"));

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

        // Verify OpenAI service was called with proper parameters
        Mockito.verify(openAiService).processRequest(Mockito.argThat(request ->
            request.text().equals("Hello with image!") &&
            request.imageUrl() != null &&
            request.imageUrl().contains(".png")
        ));

        // Verify response contains OpenAI result
        assertThat(response.processMessage().value()).contains("Image processed successfully!");
    }

}