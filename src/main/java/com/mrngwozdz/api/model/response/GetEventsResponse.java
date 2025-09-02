package com.mrngwozdz.api.model.response;

import com.mrngwozdz.api.model.EventGroupDTO;
import com.mrngwozdz.database.AppEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record GetEventsResponse(
        List<EventGroupDTO> events
) {
}