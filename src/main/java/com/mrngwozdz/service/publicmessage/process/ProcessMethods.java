package com.mrngwozdz.service.publicmessage.process;

import com.mrngwozdz.integration.openai.OpenAiService;
import com.mrngwozdz.integration.openai.model.OpenAiProcessRequest;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.service.minio.MinioService;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

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

    public static Either<Failure, String> callOpenAiService(OpenAiProcessRequest request, OpenAiService openAiService) {
        log.info("Calling OpenAI service with request");
        return openAiService.processRequest(request);
    }
}