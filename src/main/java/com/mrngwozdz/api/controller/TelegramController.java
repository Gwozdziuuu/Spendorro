package com.mrngwozdz.api.controller;

import com.mrngwozdz.api.TelegramApi;
import com.mrngwozdz.api.model.request.TelegramMessageRequest;
import com.mrngwozdz.platform.http.RestResults;
import com.mrngwozdz.service.telegram.TelegramService;
import com.mrngwozdz.service.telegram.data.api.model.TelegramUpdate;

import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;

public class TelegramController implements TelegramApi {

    private final TelegramService telegramService;

    @Inject
    public TelegramController(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public RestResponse<?> sendMessage(TelegramMessageRequest request) {
        var response = telegramService.sendMessage(request.getChatId(), request.getText());
        return RestResults.from(response);
    }

    @Override
    public RestResponse<?> receiveWebhook(TelegramUpdate update) {
        var response = telegramService.processWebhook(update);
        return RestResults.from(response);
    }

}