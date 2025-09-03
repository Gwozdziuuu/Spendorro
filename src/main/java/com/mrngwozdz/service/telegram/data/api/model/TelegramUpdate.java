package com.mrngwozdz.service.telegram.data.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUpdate(
        @JsonProperty("update_id") Long updateId,
        TelegramMessage message
) {}