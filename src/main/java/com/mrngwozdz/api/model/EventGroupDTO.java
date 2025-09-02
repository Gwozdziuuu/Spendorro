package com.mrngwozdz.api.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@RegisterForReflection
public record EventGroupDTO(
        @Schema(description = "Unique identifier for grouping related events", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID serial,
        
        @Schema(description = "Name of the method that was executed", example = "getEvents")
        String methodName,
        
        @Schema(description = "Total duration of the request in milliseconds", example = "150")
        Long duration,
        
        @Schema(description = "Status of the request execution", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILURE"})
        String status,
        
        @Schema(description = "List of events that belong to this group")
        List<EventItemDTO> events
) {
}
