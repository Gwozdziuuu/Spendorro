package com.mrngwozdz.integration.openai;

import com.mrngwozdz.common.annotation.LogRequestResponse;
import com.mrngwozdz.integration.openai.model.OpenAiProcessRequest;
import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
@LogRequestResponse
public class OpenAiService {

    private final String openAiUrl;
    private final Client client;

    @Inject
    public OpenAiService(@ConfigProperty(name = "openai.url") String openAiUrl) {
        this.openAiUrl = openAiUrl;
        this.client = ClientBuilder.newClient();
    }

    public Either<Failure, String> processRequest(OpenAiProcessRequest request) {
        log.info("Sending request to OpenAI service at: {}", openAiUrl);

        try {
            Response response = client
                .target(openAiUrl + "/process")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

            if (response.getStatus() == 200) {
                String responseBody = response.readEntity(String.class);
                log.info("Successfully received response from OpenAI service");
                return Either.right(responseBody);
            } else {
                String responseBody = null;
                try {
                    responseBody = response.readEntity(String.class);
                } catch (Exception e) {
                    log.warn("Failed to read error response body: {}", e.getMessage());
                }

                String errorMessage = String.format("OpenAI service returned status: %d, body: %s",
                    response.getStatus(), responseBody != null ? responseBody : "unable to read");
                log.error(errorMessage);
                return Either.left(Failure.of(ErrorCode.IO_ERROR, errorMessage));
            }
        } catch (Exception e) {
            log.error("Failed to call OpenAI service: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.IO_ERROR, "Failed to call OpenAI service: " + e.getMessage()));
        }
    }
}