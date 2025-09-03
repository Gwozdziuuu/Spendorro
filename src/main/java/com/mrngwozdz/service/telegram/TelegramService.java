package com.mrngwozdz.service.telegram;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import com.mrngwozdz.service.telegram.data.api.TelegramClient;
import com.mrngwozdz.service.telegram.data.api.model.TelegramMessageRequest;
import com.mrngwozdz.service.telegram.data.api.model.TelegramUpdate;
import com.mrngwozdz.service.telegram.utils.SendMessageUtils;
import io.vavr.control.Either;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.PostConstruct;

@ApplicationScoped
public class TelegramService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);

    private final TelegramClient telegramClient;
    private final String botToken;
    private final String webhookUrl;

    @Inject
    public TelegramService(
            @RestClient TelegramClient telegramClient,
            @ConfigProperty(name = "telegram.bot.token") String botToken,
            @ConfigProperty(name = "telegram.webhook.url") String webhookUrl) {
        this.telegramClient = telegramClient;
        this.botToken = botToken;
        this.webhookUrl = webhookUrl;
    }

    @PostConstruct
    public void setupWebhook() {
        if (webhookUrl != null && !webhookUrl.trim().isEmpty() && !webhookUrl.contains("localhost")) {
            Either<Failure, Success<String>> result = setWebhook(webhookUrl + "/telegram/webhook");
            if (result.isLeft()) {
                logger.warn("Failed to setup webhook: {}", result.getLeft().message());
            } else {
                logger.info("Webhook setup successfully: {}", webhookUrl + "/telegram/webhook");
            }
        } else {
            logger.info("Webhook URL not configured or is localhost - skipping webhook setup");
        }
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
            logger.info("Received Telegram webhook: updateId={}, messageId={}", 
                    update.updateId(), 
                    update.message() != null ? update.message().messageId() : null);

            if (update.message() != null && update.message().text() != null) {
                var message = update.message();
                logger.info("Processing message from user {} ({}): {}", 
                        message.from().firstName(),
                        message.from().username(),
                        message.text());

                String response = processIncomingMessage(message.text(), message.from().firstName());
                
                if (response != null) {
                    return sendMessage(message.chat().id().toString(), response);
                }
            }

            return Either.right(Success.of("Webhook processed successfully"));
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return Either.left(
                Failure.of(ErrorCode.UNKNOWN, "Error processing webhook").with("exception", e.getMessage())
            );
        }
    }

    private String processIncomingMessage(String text, String firstName) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String lowerText = text.toLowerCase().trim();
        
        if (lowerText.equals("/start")) {
            return String.format("Witaj %s! 👋\nJestem botem Spendorro - pomogę Ci zarządzać finansami.", firstName);
        }
        
        if (lowerText.equals("/help")) {
            return "Dostępne komendy:\n/start - Start bota\n/help - Pomoc\n\nMożesz też po prostu napisać wydatek, np: 'kawa 5'";
        }

        if (text.matches(".*\\d+.*")) {
            return "Widzę, że piszesz o wydatku! 💰\nFunkcja zapisywania wydatków będzie wkrótce dostępna.";
        }

        return String.format("Dziękuję za wiadomość, %s! 😊\nNa razie jestem w fazie rozwoju.", firstName);
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

    public Either<Failure, Success<String>> deleteWebhook() {
        try (Response response = telegramClient.deleteWebhook(botToken)) {
            return SendMessageUtils.processResponse(response);
        } catch (Exception e) {
            return Either.left(
                Failure.of(ErrorCode.UNKNOWN, "Error deleting webhook").with("exception", e.getMessage())
            );
        }
    }
}