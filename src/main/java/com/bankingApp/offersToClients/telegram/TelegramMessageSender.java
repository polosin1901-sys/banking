package com.bankingApp.offersToClients.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class TelegramMessageSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageSender.class);

    private final ObjectProvider<TelegramBot> telegramBot;

    public TelegramMessageSender(ObjectProvider<TelegramBot> telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendMessage(Long chatId, String text) {
        TelegramBot bot = telegramBot.getIfAvailable();
        if (bot == null) {
            log.warn("Telegram bot is not configured, message to {} was not sent", chatId);
            return;
        }

        SendResponse response = bot.execute(new SendMessage(chatId, text));
        if (!response.isOk()) {
            log.error("Failed to send message to {}: {}", chatId, response.description());
        }
    }
}