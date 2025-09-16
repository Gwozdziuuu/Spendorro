package com.mrngwozdz.api.controller;

import com.mrngwozdz.api.EventsApi;
import com.mrngwozdz.api.model.EventGroupDTO;
import com.mrngwozdz.api.model.response.GetEventsResponse;
import com.mrngwozdz.common.annotation.LogRequestResponse;
import com.mrngwozdz.service.appevent.AppEventService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

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


}