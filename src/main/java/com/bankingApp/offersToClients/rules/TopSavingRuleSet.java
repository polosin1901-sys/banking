package com.bankingApp.offersToClients.rules;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.TransactionSums;
import com.bankingApp.offersToClients.model.TransactionType;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TopSavingRuleSet implements RecommendationRuleSet {

    private final RecommendationsRepository recommendationsRepository;

    public TopSavingRuleSet(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {

        boolean hasDebit = recommendationsRepository.countTransactions(userId, ProductType.DEBIT) > 0;
        int sumDebitDeposit = recommendationsRepository.sumTransactions(
                userId, ProductType.DEBIT, TransactionType.DEPOSIT);
        int sumSavingDeposit = recommendationsRepository.sumTransactions(
                userId, ProductType.SAVING, TransactionType.DEPOSIT);
        TransactionSums debitSums = recommendationsRepository.sumDepositAndWithdraw(
                userId, ProductType.DEBIT);

        if (hasDebit
                && (sumDebitDeposit >= 50_000 || sumSavingDeposit >= 50_000)
                && debitSums.deposit() > debitSums.withdraw()) {
            return Optional.of(new RecommendationDto(
                    "Top Saving",
                    UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
                    "Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, который поможет вам легко и удобно накапливать деньги на важные цели. Больше никаких забытых чеков и потерянных квитанций — всё под контролем!\n" +
                            "\n" +
                            "Преимущества «Копилки»:\n" +
                            "\n" +
                            "Накопление средств на конкретные цели. Установите лимит и срок накопления, и банк будет автоматически переводить определенную сумму на ваш счет.\n" +
                            "\n" +
                            "Прозрачность и контроль. Отслеживайте свои доходы и расходы, контролируйте процесс накопления и корректируйте стратегию при необходимости.\n" +
                            "\n" +
                            "Безопасность и надежность. Ваши средства находятся под защитой банка, а доступ к ним возможен только через мобильное приложение или интернет-банкинг.\n" +
                            "\n" +
                            "Начните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!"
            ));
        }
        return Optional.empty();
    }
}
