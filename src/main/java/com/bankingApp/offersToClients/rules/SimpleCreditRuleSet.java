package com.bankingApp.offersToClients.rules;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleCreditRuleSet implements RecommendationRuleSet {

    private final RecommendationsRepository recommendationsRepository;

    public SimpleCreditRuleSet(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasNoTransactions = !recommendationsRepository.hasTransactionsOfType(userId,
                "CREDIT");
        int sumDebitDeposit = recommendationsRepository.getSumTransactions(userId, "DEBIT",
                "DEBIT");
        int sumDebitWithdraw = recommendationsRepository.getSumTransactions(userId, "DEBIT",
                "WITHDRAW");
        if (hasNoTransactions && (sumDebitDeposit > sumDebitWithdraw) && sumDebitDeposit > 100_000) {
            return Optional.of(new RecommendationDto(
                    "Простой кредит",
                    UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f"),
                    "Откройте мир выгодных кредитов с нами!\n" +
                            "\n" +
                            "Ищете способ быстро и без лишних хлопот получить нужную сумму? Тогда наш выгодный кредит — именно то, что вам нужно! Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.\n" +
                            "\n" +
                            "Почему выбирают нас:\n" +
                            "\n" +
                            "Быстрое рассмотрение заявки. Мы ценим ваше время, поэтому процесс рассмотрения заявки занимает всего несколько часов.\n" +
                            "\n" +
                            "Удобное оформление. Подать заявку на кредит можно онлайн на нашем сайте или в мобильном приложении.\n" +
                            "\n" +
                            "Широкий выбор кредитных продуктов. Мы предлагаем кредиты на различные цели: покупку недвижимости, автомобиля, образование, лечение и многое другое.\n" +
                            "\n" +
                            "Не упустите возможность воспользоваться выгодными условиями кредитования от нашей компании!"
            ));
        }
        return Optional.empty();
    }
}
