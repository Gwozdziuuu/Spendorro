package com.mrngwozdz.service.publicmessage;

import com.mrngwozdz.api.model.request.FileUploadRequest;
import com.mrngwozdz.api.model.request.MessageRequest;
import com.mrngwozdz.api.model.response.ProcessMessage;
import com.mrngwozdz.integration.openai.OpenAiService;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.minio.MinioService;
import com.mrngwozdz.service.publicmessage.mapper.FileUploadRequestMapper;
import com.mrngwozdz.service.publicmessage.process.ProcessSteps;
import com.mrngwozdz.service.publicmessage.process.ProcessValidator;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static com.mrngwozdz.service.publicmessage.process.ProcessValidator.validateRequest;

@Slf4j
@ApplicationScoped
public class PublicMessageService {

    private final MinioService minioService;
    private final OpenAiService openAiService;
    private final String openAiToken;
    private final String openAiModel;

    @Inject
    public PublicMessageService(MinioService minioService, OpenAiService openAiService,
                               @ConfigProperty(name = "openai.token") String openAiToken,
                               @ConfigProperty(name = "openai.model") String openAiModel) {
        this.minioService = minioService;
        this.openAiService = openAiService;
        this.openAiToken = openAiToken;
        this.openAiModel = openAiModel;
    }

    public Either<Failure, Success<ProcessMessage>> process(MessageRequest request) {
        return validateRequest(request)
                .map(h -> h.setMinioService(minioService).setOpenAiService(openAiService))
                .map(h -> h.setToken(openAiToken).setModel(openAiModel))
                .flatMap(ProcessSteps::uploadImageToStorage)
                .flatMap(ProcessSteps::buildOpenAiRequest)
                .flatMap(ProcessSteps::callOpenAiService)
                .map(response -> Success.of(new ProcessMessage(response)));
    }


    public Either<Failure, Success<ProcessMessage>> process(FileUploadRequest request) {
        log.info("Processing file upload request - fileName: {}, hasFile: {}, hasText: {}",
                request.fileName, request.file != null, request.text != null);

        return FileUploadRequestMapper.INSTANCE.toMessageRequestWithFileData(request)
                .flatMap(ProcessValidator::validateRequest)
                .map(h -> h.setMinioService(minioService).setOpenAiService(openAiService))
                .map(h -> h.setToken(openAiToken).setModel(openAiModel))
                .flatMap(ProcessSteps::uploadImageToStorage)
                .flatMap(ProcessSteps::buildOpenAiRequest)
                .flatMap(ProcessSteps::callOpenAiService)
                .map(response -> Success.of(new ProcessMessage(response)));
    }

}
