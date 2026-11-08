package com.bankingApp.offersToClients.rules;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.TransactionType;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class Invest500RuleSet implements RecommendationRuleSet {

    private final RecommendationsRepository recommendationsRepository;

    public Invest500RuleSet(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasDebit = recommendationsRepository.countTransactions(userId, ProductType.DEBIT) > 0;
        boolean hasNoInvest = recommendationsRepository.countTransactions(userId, ProductType.INVEST) == 0;
        int sumSavingDeposit = recommendationsRepository.sumTransactions(
                userId, ProductType.SAVING, TransactionType.DEPOSIT);

        if (hasDebit && hasNoInvest && sumSavingDeposit > 1000) {
            return Optional.of(new RecommendationDto(
                    "Invest500",
                    UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                    "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! " +
                            "Воспользуйтесь налоговыми льготами и начните инвестировать с умом. Пополните счет до конца " +
                            "года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. Не упустите " +
                            "возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными " +
                            "тенденциями. Откройте ИИС сегодня и станьте ближе к финансовой независимости!"
            ));
        }
        return Optional.empty();
    }
}
