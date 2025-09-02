package com.mrngwozdz.api.controller;

import com.mrngwozdz.api.EventsApi;
import com.mrngwozdz.api.model.EventGroupDTO;
import com.mrngwozdz.api.model.response.GetEventsResponse;
import com.mrngwozdz.common.annotation.LogRequestResponse;
import com.mrngwozdz.service.appevent.AppEventService;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestSseElementType;

import java.util.List;

@LogRequestResponse
@RequiredArgsConstructor
public class EventController implements EventsApi {

    private final AppEventService appEventService;

    @Override
    public Response getEvents(int limit) {
        List<EventGroupDTO> eventGroups = appEventService.getRecentEventGroups(limit);
        GetEventsResponse response = new GetEventsResponse(eventGroups);
        return Response.ok(response).build();
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    @io.smallrye.common.annotation.NonBlocking
    public Multi<String> streamEvents() {
        return appEventService.getEventStream();
    }

}