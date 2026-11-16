package com.bankingApp.offersToClients.telegram;

import com.pengrad.telegrambot.TelegramBot;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Creates the Telegram bot client only when a bot token is configured.
 *
 * <p>The token is supplied through the {@code TELEGRAM_BOT_TOKEN} environment variable, so the
 * application and its tests start normally on machines where the bot is not needed.</p>
 */
@Configuration
public class TelegramBotConfiguration {

    /**
     * Builds a {@link TelegramBot} for long polling.
     *
     * <p>If {@code telegram.bot.proxy.host} and {@code telegram.bot.proxy.port} are set, all bot
     * requests go through that HTTP proxy. This is required in networks where the JVM has no
     * direct access to {@code api.telegram.org}.</p>
     *
     * @param token     bot token issued by BotFather
     * @param proxyHost optional proxy host, empty when no proxy is used
     * @param proxyPort optional proxy port, {@code 0} when no proxy is used
     * @return configured bot client
     */
    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot", name = "token")
    public TelegramBot telegramBot(@Value("${telegram.bot.token}") String token,
                                   @Value("${telegram.bot.proxy.host:}") String proxyHost,
                                   @Value("${telegram.bot.proxy.port:0}") int proxyPort) {
        TelegramBot.Builder builder = new TelegramBot.Builder(token);

        if (!proxyHost.isBlank() && proxyPort > 0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            builder.okHttpClient(new OkHttpClient.Builder().proxy(proxy).build());
        }

        return builder.build();
    }
}
