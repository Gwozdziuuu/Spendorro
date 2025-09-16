package com.mrngwozdz.service.publicmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.api.model.request.FileUploadRequest;
import com.mrngwozdz.api.model.request.MessageRequest;
import com.mrngwozdz.api.model.response.ProcessMessage;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.minio.MinioService;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class PublicMessageService {

    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    @Inject
    public PublicMessageService(MinioService minioService, ObjectMapper objectMapper) {
        this.minioService = minioService;
        this.objectMapper = objectMapper;
    }

    public Either<Failure, Success<ProcessMessage>> process(MessageRequest request) {
        try {
            log.info("Processing MessageRequest - text: {}, hasImageData: {}, imageFilename: {}",
                    request.getText(), request.getImageData() != null, request.getImageFilename());

            String imageUrl = null;

            // If image data is provided, upload to MinIO
            if (request.getImageData() != null) {
                try {
                    String fileName = request.getImageFilename() != null ? request.getImageFilename() : "image_" + System.currentTimeMillis() + ".jpg";
                    String contentType = getContentType(fileName);

                    ByteArrayInputStream imageStream = new ByteArrayInputStream(request.getImageData());
                    var objectName = minioService.uploadImage(imageStream, fileName, contentType).get();

                    // Get temporary URL (5 minutes)
                    imageUrl = minioService.getShortPresignedUrl(objectName);

                    log.info("Successfully uploaded image to MinIO: {}, URL: {}", objectName, imageUrl);
                } catch (Exception e) {
                    log.error("Failed to upload image to MinIO: {}", e.getMessage());
                    return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to upload image: " + e.getMessage()));
                }
            }

            // Build the JSON object
            Map<String, Object> responseObject = new HashMap<>();
            responseObject.put("text", request.getText());
            responseObject.put("token", request.getToken());
            responseObject.put("model", request.getModel());
            responseObject.put("image_url", imageUrl != null ? imageUrl : request.getImageUrl());

            if (request.getOutputExample() != null) {
                responseObject.put("output_example", request.getOutputExample());
            }

            String jsonResponse = objectMapper.writeValueAsString(responseObject);
            log.info("Built JSON object: {}", jsonResponse);

            return Either.right(Success.of(new ProcessMessage(jsonResponse)));
        } catch (Exception e) {
            log.error("Failed to process MessageRequest: {}", e.getMessage());
            log.debug("Full stacktrace:", e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to process message request: " + e.getMessage()));
        }
    }

    private String getContentType(String fileName) {
        if (fileName == null) return "image/jpeg";

        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".png")) return "image/png";
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerFileName.endsWith(".gif")) return "image/gif";

        return "image/jpeg"; // default
    }

    public Either<Failure, Success<ProcessMessage>> process(FileUploadRequest request) {
        log.info("Processing file upload request - fileName: {}, hasFile: {}, hasText: {}",
                request.fileName, request.file != null, request.text != null);

        if (request.fileName != null) {
            String fileName = request.fileName.toLowerCase();
            if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                log.warn("Invalid file type: {}", fileName);
                return Either.left(Failure.of(ErrorCode.INVALID_FILE_TYPE, "Unsupported file format. Supported formats: PNG, JPG"));
            }
        }

        try {
            String imageUrl = null;

            // If file is provided, upload to MinIO
            if (request.file != null) {
                String fileName = request.fileName != null ? request.fileName : "uploaded_image_" + System.currentTimeMillis() + ".jpg";
                String contentType = getContentType(fileName);

                log.info("Uploading file to MinIO: {}", fileName);
                var objectName = minioService.uploadImage(request.file, fileName, contentType).get();

                // Get public URL for the uploaded image
                imageUrl = minioService.getPublicUrl(objectName);
                log.info("Successfully uploaded file to MinIO: {}, URL: {}", objectName, imageUrl);
            }

            // Build response with image URL and text
            Map<String, Object> responseObject = new HashMap<>();
            responseObject.put("message", String.format("Successfully processed file '%s'%s",
                    request.fileName,
                    request.text != null ? " with text: " + request.text : ""));
            responseObject.put("image_url", imageUrl);
            if (request.text != null) {
                responseObject.put("text", request.text);
            }

            String jsonResponse = objectMapper.writeValueAsString(responseObject);
            log.info("Generated response with image URL: {}", jsonResponse);

            return Either.right(Success.of(new ProcessMessage(jsonResponse)));
        } catch (Exception e) {
            log.error("Failed to process image: {}", e.getMessage());
            log.debug("Full stacktrace:", e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to process image: " + e.getMessage()));
        }
    }

}
