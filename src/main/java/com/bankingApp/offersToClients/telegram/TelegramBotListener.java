package com.bankingApp.offersToClients.telegram;

import com.bankingApp.offersToClients.telegram.service.TelegramBotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Receives Telegram updates through long polling and routes them to {@link TelegramBotService}.
 *
 * <p>The listener registers itself only when a {@link TelegramBot} bean exists, so the application
 * keeps working without a bot token.</p>
 */
@Component
public class TelegramBotListener implements UpdatesListener, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotListener.class);

    private static final String RECOMMEND_COMMAND = "/recommend";
    private static final String BOT_NAME_SEPARATOR = "@";

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

    /**
     * Starts long polling after all dependencies are injected.
     */
    @Override
    public void afterPropertiesSet() {
        TelegramBot bot = telegramBot.getIfAvailable();
        if (bot == null) {
            log.info("Telegram bot is not configured, long polling is disabled");
            return;
        }

        bot.setUpdatesListener(this, exception -> log.error("Telegram polling error", exception));
        log.info("Telegram bot long polling started");
    }

    /**
     * Processes a batch of updates and confirms them all.
     *
     * @param updates updates received from Telegram
     * @return confirmation flag that lets Telegram drop the processed updates
     */
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
        String[] parts = text.split("\\s+", 2);
        String command = parts[0];
        String argument = parts.length > 1 ? parts[1].trim() : "";

        if (isRecommendCommand(command)) {
            messageSender.sendMessage(chatId, telegramBotService.recommendation(argument));
        } else {
            messageSender.sendMessage(chatId, telegramBotService.help());
        }
    }

    /**
     * Matches both {@code /recommend} and the group form {@code /recommend@bot_name}.
     */
    private boolean isRecommendCommand(String command) {
        return command.equals(RECOMMEND_COMMAND)
                || command.startsWith(RECOMMEND_COMMAND + BOT_NAME_SEPARATOR);
    }
}
