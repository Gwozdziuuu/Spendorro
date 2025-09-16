package com.mrngwozdz.api;

import com.mrngwozdz.api.model.request.MessageRequest;
import com.mrngwozdz.api.model.request.FileUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name = "Public API", description = "Public API for application communication")
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public interface PublicApi {

    @Operation(
            summary = "Process message from user",
            description = "Processes a text message from user and returns response",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message processed successfully",
                            content = @Content(schema = @Schema(implementation = Response.class))
                    )
            }
    )
    @POST
    @Path("/message")
    @Consumes(MediaType.APPLICATION_JSON)
    Response processMessage(MessageRequest request);

    @Operation(
            summary = "Upload picture with optional message",
            description = "Uploads a picture file (PNG or JPG only) with optional text message",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Picture uploaded and processed successfully",
                            content = @Content(schema = @Schema(implementation = Response.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file type - only PNG and JPG are supported",
                            content = @Content(schema = @Schema(implementation = Response.class))
                    )
            }
    )
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadPicture(FileUploadRequest request);

}