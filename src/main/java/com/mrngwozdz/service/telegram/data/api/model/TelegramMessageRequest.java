package com.mrngwozdz.service.telegram.data.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramMessageRequest {
    
    @JsonProperty("chat_id")
    private String chatId;
    
    private String text;
    
    public TelegramMessageRequest(String chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }
}