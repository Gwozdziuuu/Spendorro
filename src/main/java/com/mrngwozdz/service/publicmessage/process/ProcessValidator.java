package com.mrngwozdz.service.publicmessage.process;

import com.mrngwozdz.api.model.request.MessageRequest;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import io.vavr.control.Either;

import java.util.Optional;

public class ProcessValidator {

    private ProcessValidator() {}

    public static Either<Failure, ProcessHelper> validateRequest(MessageRequest request) {
        return RequestValidator.validate(request)
                .flatMap(TextValidator::validate)
                .flatMap(ImageValidator::validate);
    }

    private static class RequestValidator {
        static Either<Failure, MessageRequest> validate(MessageRequest request) {
            if (request == null) {
                return Either.left(Failure.of(ErrorCode.VALIDATION, "Request is null"));
            }
            return Either.right(request);
        }
    }

    private static class TextValidator {
        static Either<Failure, MessageRequest> validate(MessageRequest request) {
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                return Either.left(Failure.of(ErrorCode.VALIDATION, "Text is required"));
            }
            return Either.right(request);
        }
    }

    private static class ImageValidator {
        static Either<Failure, ProcessHelper> validate(MessageRequest request) {
            var helper = new ProcessHelper()
                    .setText(request.getText())
                    .setOutputExample("");

            return validateImage(request.getImageData(), request.getImageFilename())
                    .map(optionalImage -> {
                        if (optionalImage.isPresent()) {
                            return helper.setImage(optionalImage.get());
                        }
                        return helper;
                    });
        }

        private static Either<Failure, Optional<ProcessHelper.Image>> validateImage(byte[] imageData, String imageFilename) {
            // Both null - no image provided, which is OK
            if (imageData == null && imageFilename == null) {
                return Either.right(Optional.empty());
            }

            // One is null but not the other - invalid state
            if (imageData == null || imageFilename == null) {
                return Either.left(Failure.of(ErrorCode.VALIDATION, "Both imageData and imageFilename must be provided together or both null"));
            }

            // Check if filename has valid extension
            String lowerFilename = imageFilename.toLowerCase();
            if (!lowerFilename.endsWith(".png") && !lowerFilename.endsWith(".jpg") && !lowerFilename.endsWith(".jpeg")) {
                return Either.left(Failure.of(ErrorCode.INVALID_FILE_TYPE, "Only PNG, JPG and JPEG files are supported"));
            }

            // Check if imageData is not empty
            if (imageData.length == 0) {
                return Either.left(Failure.of(ErrorCode.VALIDATION, "Image data cannot be empty"));
            }

            return Either.right(Optional.of(new ProcessHelper.Image(imageData, imageFilename)));
        }
    }

}
