package com.bankingApp.offersToClients.telegram.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.model.UserInfo;
import com.bankingApp.offersToClients.repository.UsersRepository;
import com.bankingApp.offersToClients.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the text replies of the Telegram bot.
 *
 * <p>Recommendations are produced by the same {@link RecommendationService} as the REST API,
 * therefore dynamic rule hits caused by the bot are counted in the rule statistics.</p>
 */
@Service
public class TelegramBotService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String NO_PRODUCTS = "— пока нет подходящих продуктов";

    private final UsersRepository usersRepository;
    private final RecommendationService recommendationService;

    public TelegramBotService(UsersRepository usersRepository,
                              RecommendationService recommendationService) {
        this.usersRepository = usersRepository;
        this.recommendationService = recommendationService;
    }

    /**
     * @return greeting and command reference shown for {@code /start} and unknown commands
     */
    public String help() {
        return """
                Привет! Я помогу подобрать банковские продукты.

                Доступные команды:
                /recommend <username> — рекомендации для клиента

                Пример: /recommend quintin.deckow
                """;
    }

    /**
     * Looks up a client by username and formats personal recommendations.
     *
     * @param username Telegram username without or with a leading {@code @}
     * @return greeting with the product list, or {@code Пользователь не найден} when the client
     *         does not exist or several clients share the username
     */
    public String recommendation(String username) {
        return usersRepository.findByUsername(normalize(username))
                .map(this::formatRecommendations)
                .orElse(USER_NOT_FOUND);
    }

    private String formatRecommendations(UserInfo user) {
        List<RecommendationDto> recommendations =
                recommendationService.getRecommendations(user.id());

        String products = recommendations.isEmpty()
                ? NO_PRODUCTS
                : recommendations.stream()
                        .map(recommendation ->
                                "• " + recommendation.name() + "\n" + recommendation.text())
                        .collect(Collectors.joining("\n\n"));

        return "Здравствуйте, " + user.firstName() + " " + user.lastName() + "!\n"
                + "Новые продукты для вас:\n"
                + products;
    }

    private String normalize(String username) {
        if (username == null) {
            return "";
        }

        String trimmed = username.trim();
        return trimmed.startsWith("@") ? trimmed.substring(1) : trimmed;
    }
}
