package com.mrngwozdz.service.telegram;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.telegram.data.api.TelegramClient;
import com.mrngwozdz.service.telegram.data.api.model.TelegramMessageRequest;
import com.mrngwozdz.service.telegram.utils.SendMessageUtils;
import io.vavr.control.Either;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class TelegramService {

    private final TelegramClient telegramClient;
    private final String botToken;

    @Inject
    public TelegramService(
            @RestClient TelegramClient telegramClient,
            @ConfigProperty(name = "telegram.bot.token") String botToken) {
        this.telegramClient = telegramClient;
        this.botToken = botToken;
    }

    public Either<Failure, Success<String>> sendMessage(String chatId, String text) {
        var request = new TelegramMessageRequest(chatId, text);
        try (Response response = telegramClient.sendMessage(botToken, request)) {
            return SendMessageUtils.processResponse(response);
        } catch (Exception e) {
            return Either.left(
                Failure.of(ErrorCode.UNKNOWN, "Error sending message").with("exception", e.getMessage())
            );
        }
    }
}