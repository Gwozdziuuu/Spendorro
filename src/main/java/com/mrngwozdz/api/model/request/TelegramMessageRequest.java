package com.mrngwozdz.api.model.request;

import lombok.Data;

@Data
public class TelegramMessageRequest {
    private String chatId;
    private String text;
}