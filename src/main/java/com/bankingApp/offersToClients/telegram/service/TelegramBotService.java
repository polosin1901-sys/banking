package com.bankingApp.offersToClients.telegram.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.model.UserInfo;
import com.bankingApp.offersToClients.repository.UsersRepository;
import com.bankingApp.offersToClients.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TelegramBotService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final UsersRepository usersRepository;
    private final RecommendationService recommendationService;

    public TelegramBotService(UsersRepository usersRepository, RecommendationService recommendationService) {
        this.usersRepository = usersRepository;
        this.recommendationService = recommendationService;
    }

    public String help() {
        return """
                Привет! Я помогу подобрать банковские продукты.
                
                Доступные команды:
                /recommend <username> — рекомендации для клиента
                
                Пример: /recommend quintin.deckow
                """;
    }

    private String normalize(String userName) {
        if (userName == null) {
            return "";
        }
        String trimmed = userName.trim();
        return trimmed.startsWith("@") ? trimmed.substring(1) : trimmed;
    }

    private String formatRecommendations(UserInfo user) {
        List<RecommendationDto> recommendations =
                recommendationService.getRecommendations(user.id());

        String products = recommendations.isEmpty()
                ? "— пока нет подходящих продуктов"
                : recommendations.stream()
                  .map(recommendation ->
                       "• " + recommendation.name() + "\n" + recommendation.text())
                  .collect(Collectors.joining("\n\n"));

        return "Здравствуйте, " + user.firstName() + " " + user.lastName() + "!\n"
                + "Новые продукты для вас:\n"
                + products;
    }

    public String recommendation(String username) {
        return usersRepository.findByUsername(normalize(username))
                .map(this::formatRecommendations)
                .orElse(USER_NOT_FOUND);
    }
}
