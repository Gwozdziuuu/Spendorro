package com.mrngwozdz.service.telegram.data.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessage(
        @JsonProperty("message_id") Long messageId,
        TelegramUser from,
        TelegramChat chat,
        Long date,
        String text
) {}