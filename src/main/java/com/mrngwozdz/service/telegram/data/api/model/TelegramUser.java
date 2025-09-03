package com.mrngwozdz.service.telegram.data.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUser(
        Long id,
        @JsonProperty("is_bot") Boolean isBot,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("language_code") String languageCode
) {}