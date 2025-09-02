package com.mrngwozdz.api.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@RegisterForReflection
public record EventItemDTO(
        @Schema(description = "Type of the event", example = "API_REQUEST")
        String eventType,
        
        @Schema(description = "Description of the event", example = "API call to EventController.getEvents")
        String description,
        
        @Schema(description = "JSON data containing event details", example = "{\"parameters\":[50]}")
        String eventData,
        
        @Schema(description = "Timestamp when the event was created")
        Instant createdAt
) {
}