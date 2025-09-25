package com.mrngwozdz.behavioral;

import com.mrngwozdz.AbstractIntegrationTest;
import com.mrngwozdz.api.model.response.ProcessMessageResponse;
import com.mrngwozdz.controller.PublicControllerUtils;
import com.mrngwozdz.integration.openai.OpenAiService;
import com.mrngwozdz.integration.openai.model.OpenAiProcessRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PublicControllerTest extends AbstractIntegrationTest {

    @InjectMock
    OpenAiService openAiService;

    @ParameterizedTest
    @MethodSource("openAiResponsesWithExpectedResults")
    void shouldHandleVariousOpenAiResponses(String openAiResponse, String expectedResult) {
        // Mock OpenAI service response
        Mockito.when(openAiService.processRequest(Mockito.any(OpenAiProcessRequest.class)))
                .thenReturn(Either.right(openAiResponse));

        var request = """
                    {"text": "Hello World!"}
                    """;
        var response = PublicControllerUtils.processMessage(request)
                .statusCode(200).log().all()
                .extract().as(ProcessMessageResponse.class);

        // Verify OpenAI service was called
        Mockito.verify(openAiService).processRequest(Mockito.any(OpenAiProcessRequest.class));

        // Verify the response contains exactly the expected result
        assertThat(response.processMessage().value()).contains(expectedResult);
    }

    static Stream<Arguments> openAiResponsesWithExpectedResults() {
        return Stream.of(
                Arguments.of(
                        """
                        {"response": "Hello response from OpenAI!"}
                        """,
                        """
                        Hello response from OpenAI!"""
                ),
                Arguments.of(
                        """
                        {"response": "Polskie znaki: ąćęłńóśźż ĄĆĘŁŃÓŚŹŻ"}
                        """,
                        """
                        Polskie znaki: ąćęłńóśźż ĄĆĘŁŃÓŚŹŻ"""
                ),
                Arguments.of(
                        """
                        {"response": "Complex JSON response with special chars: @#$%^&*()"}
                        """,
                        """
                        Complex JSON response with special chars: @#$%^&*()"""
                ),
                Arguments.of(
                        """
                        {"response": "Multi-line\\nresponse\\nwith\\nnewlines"}
                        """,
                        """
                        Multi-line
                        response
                        with
                        newlines"""
                ),
                Arguments.of(
                        """
                        {"response": "Response with \\"quotes\\" and backslashes\\\\"}
                        """,
                        """
                        Response with "quotes" and backslashes\\"""
                ),
                Arguments.of(
                        """
                        {"response": "Unicode characters: 🚀 ✨ 🎉 ❤️"}
                        """,
                        """
                        Unicode characters: 🚀 ✨ 🎉 ❤️"""
                ),
                Arguments.of(
                        """
                        {"response": "Mixed: Cześć! How are you? 你好"}
                        """,
                        """
                        Mixed: Cześć! How are you? 你好"""
                ),
                Arguments.of(
                        """
                        {"has_image":true,"model_used":"gpt-4o","response":"To jest zrzut ekranu pokazuj\\u0105cy status dzia\\u0142ania czterech instancji lub us\\u0142ug, kt\\u00f3re s\\u0105 uruchomione przez 7 minut. Mo\\u017ce to dotyczy\\u0107 np. kontener\\u00f3w Docker, us\\u0142ug w chmurze, aplikacji serwerowych lub innych proces\\u00f3w informatycznych.","success":true,"usage":{"completion_tokens":65,"prompt_tokens":265,"total_tokens":330}}
                        """,
                        """
                        To jest zrzut ekranu pokazujący status działania czterech instancji lub usług, które są uruchomione przez 7 minut. Może to dotyczyć np. kontenerów Docker, usług w chmurze, aplikacji serwerowych lub innych procesów informatycznych."""
                )
        );
    }

    @Test
    void shouldHandleRegularMessage() {
        // Mock OpenAI service response
        Mockito.when(openAiService.processRequest(Mockito.any(OpenAiProcessRequest.class)))
                .thenReturn(Either.right("{\"response\": \"Hello response from OpenAI!\"}"));

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
                .thenReturn(Either.right("{\"response\": \"Image processed successfully!\"}"));

        // Create a minimal PNG byte array (1x1 transparent PNG)
        byte[] originalPngBytes = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 15, (byte) 0xC4,
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