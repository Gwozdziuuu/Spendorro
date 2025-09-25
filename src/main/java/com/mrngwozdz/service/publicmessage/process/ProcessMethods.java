package com.mrngwozdz.service.publicmessage.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.integration.openai.OpenAiService;
import com.mrngwozdz.integration.openai.model.OpenAiProcessRequest;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.service.minio.MinioService;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Slf4j
public class ProcessMethods {

    private ProcessMethods() {}

    public static Either<Failure, String> uploadImageToMinio(ProcessHelper.Image image, MinioService minioService) {
        String fileName = image.imageFilename() != null ? image.imageFilename() : "image_" + System.currentTimeMillis() + ".jpg";
        String contentType = getContentType(fileName);

        ByteArrayInputStream imageStream = new ByteArrayInputStream(image.imageData());
        Either<Failure, String> uploadResult = minioService.uploadImage(imageStream, fileName, contentType);

        if (uploadResult.isLeft()) {
            log.error("Failed to upload image to MinIO: {}", uploadResult.getLeft().message());
            return uploadResult;
        }

        String objectName = uploadResult.get();
        String imageUrl;

        try {
            imageUrl = minioService.getShortPresignedUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for object: {}", objectName, e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to generate presigned URL: " + e.getMessage()));
        }

        log.info("Successfully uploaded image to MinIO: {}, URL: {}", objectName, imageUrl);
        return Either.right(imageUrl);
    }

    public static Either<Failure, String> callOpenAiService(OpenAiProcessRequest request, OpenAiService openAiService) {
        log.info("Calling OpenAI service with request");
        return openAiService.processRequest(request)
                .flatMap(ProcessMethods::parseOpenAiResponse);
    }

    public static Either<Failure, OpenAiProcessRequest> buildOpenAiRequest(ProcessHelper helper) {
        log.info("Building OpenAI request");

        String outputExample = (helper.getOutputExample() != null && !helper.getOutputExample().isEmpty())
                ? helper.getOutputExample()
                : null;

        OpenAiProcessRequest request = new OpenAiProcessRequest(
                helper.getText() != null ? helper.getText() : "",
                helper.getToken(),
                helper.getModel(),
                helper.getUploadedImageURL().orElse(null),
                outputExample
        );

        log.info("Built OpenAI request: text={}, model={}, imageUrl={}",
                request.text(), request.model(), request.imageUrl());

        return Either.right(request);
    }

    private static String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private static Either<Failure, String> parseOpenAiResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            if (jsonNode.has("response")) {
                String response = jsonNode.get("response").asText();
                String decodedResponse = decodeUnicodeEscapes(response);
                log.info("Successfully parsed OpenAI response, extracted response field and decoded unicode");
                return Either.right(decodedResponse);
            } else {
                log.warn("OpenAI response does not contain 'response' field: {}", jsonResponse);
                return Either.left(Failure.of(ErrorCode.IO_ERROR, "OpenAI response missing 'response' field"));
            }
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to parse OpenAI response: " + e.getMessage()));
        }
    }

    private static String decodeUnicodeEscapes(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (i < input.length() - 5 && input.charAt(i) == '\\' && input.charAt(i + 1) == 'u') {
                try {
                    String unicodeStr = input.substring(i + 2, i + 6);
                    int codePoint = Integer.parseInt(unicodeStr, 16);
                    result.append((char) codePoint);
                    i += 6;
                } catch (NumberFormatException e) {
                    result.append(input.charAt(i));
                    i++;
                }
            } else {
                result.append(input.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
}