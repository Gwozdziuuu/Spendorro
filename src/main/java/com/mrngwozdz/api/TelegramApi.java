package com.mrngwozdz.api;

import com.mrngwozdz.api.model.request.TelegramMessageRequest;
import com.mrngwozdz.service.telegram.data.api.model.TelegramUpdate;
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

    @Operation(
            summary = "Receive webhook from Telegram",
            description = "Receives incoming messages from Telegram bot webhook",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Webhook processed successfully"
                    )
            }
    )
    @POST
    @Path("/webhook")
    RestResponse<?> receiveWebhook(TelegramUpdate update);

    @Operation(
            summary = "Get webhook info",
            description = "Get current webhook configuration from Telegram"
    )
    @GET
    @Path("/webhook-info")
    RestResponse<?> getWebhookInfo();

    @Operation(
            summary = "Test webhook endpoint",
            description = "Test webhook endpoint with sample data"
    )
    @POST
    @Path("/test-webhook")
    RestResponse<?> testWebhook();

}