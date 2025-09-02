package com.mrngwozdz.api.model.response;

import com.mrngwozdz.database.AppEvent;

public record EventResponse(
        AppEvent event
) {
}