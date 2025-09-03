package com.mrngwozdz.api;

import com.mrngwozdz.api.model.request.TelegramMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

@Tag(name = "Telegram", description = "Telegram message sending")
@Path("/telegram")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TelegramApi {

    @Operation(
            summary = "Send message to Telegram chat",
            description = "Sends a text message to specified Telegram chat",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message sent successfully",
                            content = @Content(schema = @Schema(implementation = Response.class))
                    )
            }
    )
    @POST
    @Path("/send-message")
    RestResponse<?> sendMessage(TelegramMessageRequest request);

}