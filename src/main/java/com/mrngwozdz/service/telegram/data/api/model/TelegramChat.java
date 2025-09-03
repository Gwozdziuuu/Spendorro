package com.mrngwozdz.service.telegram.data.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramChat(
        Long id,
        String type,
        String title,
        String username,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName
) {}