package com.mrngwozdz.service.publicmessage;

import com.mrngwozdz.api.model.request.FileUploadRequest;
import com.mrngwozdz.api.model.response.ProcessMessage;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PublicMessageService {

    public Either<Failure, Success<ProcessMessage>> process(String value) {
        return Either.right(Success.of(new ProcessMessage("Echo: %s".formatted(value))));
    }

    public Either<Failure, Success<ProcessMessage>> process(FileUploadRequest request) {
        if (request.fileName != null) {
            String fileName = request.fileName.toLowerCase();
            if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                return Either.left(Failure.of(ErrorCode.INVALID_FILE_TYPE, "Unsupported file format. Supported formats: PNG, JPG"));
            }
        }
        return Either.right(Success.of(new ProcessMessage("File processed successfully")));
    }

}
