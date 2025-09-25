package com.mrngwozdz.service.publicmessage.mapper;

import com.mrngwozdz.api.model.request.FileUploadRequest;
import com.mrngwozdz.api.model.request.MessageRequest;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import io.vavr.control.Either;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FileUploadRequestMapper {

    FileUploadRequestMapper INSTANCE = Mappers.getMapper(FileUploadRequestMapper.class);

    @Mapping(source = "text", target = "text")
    @Mapping(source = "fileName", target = "imageFilename")
    MessageRequest toMessageRequest(FileUploadRequest request);

    default Either<Failure, MessageRequest> toMessageRequestWithFileData(FileUploadRequest request) {
        try {
            MessageRequest messageRequest = toMessageRequest(request);

            if (request.file != null) {
                messageRequest.setImageData(request.file.readAllBytes());
            }

            return Either.right(messageRequest);
        } catch (Exception e) {
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to read file data: " + e.getMessage()));
        }
    }

}