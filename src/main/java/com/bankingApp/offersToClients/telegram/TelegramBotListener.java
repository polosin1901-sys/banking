package com.bankingApp.offersToClients.telegram;

import com.bankingApp.offersToClients.telegram.service.TelegramBotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelegramBotListener implements UpdatesListener, InitializingBean {

    private static final String RECOMMEND_COMMAND = "/recommend";

    private final ObjectProvider<TelegramBot> telegramBot;
    private final TelegramMessageSender messageSender;
    private final TelegramBotService telegramBotService;

    public TelegramBotListener(ObjectProvider<TelegramBot> telegramBot,
                               TelegramMessageSender messageSender,
                               TelegramBotService telegramBotService) {
        this.telegramBot = telegramBot;
        this.messageSender = messageSender;
        this.telegramBotService = telegramBotService;
    }

    @Override
    public void afterPropertiesSet() {
        TelegramBot bot = telegramBot.getIfAvailable();
        if (bot != null) {
            bot.setUpdatesListener(this);
        }
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(this::processUpdate);
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        Message message = update.message() != null
                ? update.message()
                : update.editedMessage();

        if (message == null || message.chat() == null || message.text() == null) {
            return;
        }

        Long chatId = message.chat().id();
        String text = message.text().trim();

        if (text.startsWith(RECOMMEND_COMMAND)) {
            String username = text.substring(RECOMMEND_COMMAND.length()).trim();
            messageSender.sendMessage(chatId, telegramBotService.recommendation(username));
        } else {
            messageSender.sendMessage(chatId, telegramBotService.help());
        }
    }
}