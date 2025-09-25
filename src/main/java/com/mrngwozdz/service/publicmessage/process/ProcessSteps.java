package com.mrngwozdz.service.publicmessage.process;

import com.mrngwozdz.platform.result.Failure;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;

import static com.mrngwozdz.service.publicmessage.process.ProcessMethods.uploadImageToMinio;

@Slf4j
public class ProcessSteps {

    private ProcessSteps() {}

    public static Either<Failure, ProcessHelper> uploadImageToStorage(ProcessHelper h) {
        if (h.getImage().isEmpty()) {
            return Either.right(h);
        }

        var image = h.getImage().get();
        return uploadImageToMinio(image, h.getMinioService()).map(h::setUploadedImageURL);
    }

    public static Either<Failure, ProcessHelper> buildOpenAiRequest(ProcessHelper h) {
        return ProcessMethods.buildOpenAiRequest(h)
                .map(h::setOpenAiProcessRequest);
    }

    public static Either<Failure, String> callOpenAiService(ProcessHelper h) {
        return ProcessMethods.callOpenAiService(h.getOpenAiProcessRequest(), h.getOpenAiService());
    }

}
