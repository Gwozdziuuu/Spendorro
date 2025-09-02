package com.mrngwozdz.api;

import com.mrngwozdz.api.model.response.GetEventsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name = "Events", description = "Application events and audit logging")
@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface EventsApi {

    @Operation(
            summary = "Get recent events",
            description = "Returns a list of recent application events",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Events retrieved successfully",
                            content = @Content(schema = @Schema(implementation = GetEventsResponse.class))
                    )
            }
    )
    @GET
    Response getEvents(
            @Parameter(description = "Maximum number of events to return")
            @QueryParam("limit") @DefaultValue("50") int limit
    );

}