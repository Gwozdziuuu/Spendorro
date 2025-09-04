package com.mrngwozdz.service.telegram;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.telegram.data.api.TelegramClient;
import com.mrngwozdz.service.telegram.data.api.model.request.TelegramMessageRequest;
import com.mrngwozdz.service.telegram.data.api.model.TelegramUpdate;
import com.mrngwozdz.service.telegram.utils.SendMessageUtils;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;

@Slf4j
@ApplicationScoped
public class TelegramService {

    private final TelegramClient telegramClient;
    private final String botToken;
    private final String webhookUrl;
    private final String botUsername;

    @Inject
    public TelegramService(
            @RestClient TelegramClient telegramClient,
            @ConfigProperty(name = "telegram.bot.token") String botToken,
            @ConfigProperty(name = "telegram.webhook.url") String webhookUrl,
            @ConfigProperty(name = "telegram.bot.username") String botUsername) {
        this.telegramClient = telegramClient;
        this.botToken = botToken;
        this.webhookUrl = webhookUrl;
        this.botUsername = botUsername;
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("=== TELEGRAM BOT STARTUP ===");
        log.info("Bot username: @{}", botUsername);
        log.info("Bot token: {}...{}",
            botToken.substring(0, Math.min(10, botToken.length())),
            botToken.length() > 10 ? botToken.substring(botToken.length() - 4) : "");
        log.info("Webhook URL: {}", webhookUrl);
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

    public Either<Failure, Success<String>> processWebhook(TelegramUpdate update) {
        try {
            log.info("Received Telegram webhook: updateId={}, messageId={}",
                    update.updateId(), 
                    update.message() != null ? update.message().messageId() : null);

            if (update.message() != null && update.message().text() != null) {
                var message = update.message();
                log.info("Processing message from user {} ({}): {}",
                        message.from().firstName(),
                        message.from().username(),
                        message.text());

                String response = "Telegram test";
                
                if (response != null) {
                    return sendMessage(message.chat().id().toString(), response);
                }
            }

            return Either.right(Success.of("Webhook processed successfully"));
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return Either.left(
                Failure.of(ErrorCode.UNKNOWN, "Error processing webhook").with("exception", e.getMessage())
            );
        }
    }

    public Either<Failure, Success<String>> setWebhook(String url) {
        try (Response response = telegramClient.setWebhook(botToken, url)) {
            return SendMessageUtils.processResponse(response);
        } catch (Exception e) {
            return Either.left(
                Failure.of(ErrorCode.UNKNOWN, "Error setting webhook").with("exception", e.getMessage())
            );
        }
    }

}