package com.mrngwozdz.api.controller;

import com.mrngwozdz.api.PublicApi;
import com.mrngwozdz.api.model.request.MessageRequest;
import com.mrngwozdz.api.model.request.FileUploadRequest;
import com.mrngwozdz.api.model.response.ProcessMessageResponse;
import com.mrngwozdz.service.publicmessage.PublicMessageService;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.Map;

public class PublicController implements PublicApi {

    private final PublicMessageService publicMessageService;

    @Inject
    public PublicController(PublicMessageService publicMessageService) {
        this.publicMessageService = publicMessageService;
    }

    @Override
    public Response processMessage(MessageRequest request) {
        var either = publicMessageService.process(request);
        return either.fold(
            failure -> Response.status(failure.getHttpStatus())
                             .entity(Map.of("error", failure.message(), "code", failure.code().name()))
                             .build(),
            success -> Response.ok(new ProcessMessageResponse(success.value())).build()
        );
    }

    @Override
    public Response uploadPicture(FileUploadRequest request) {
        var either = publicMessageService.process(request);
        
        return either.fold(
            failure -> Response.status(failure.getHttpStatus())
                             .entity(Map.of("error", failure.message(), "code", failure.code().name()))
                             .build(),
            success -> Response.ok(new ProcessMessageResponse(success.value())).build()
        );
    }

}