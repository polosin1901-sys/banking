package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RuleStatDto;
import com.bankingApp.offersToClients.entity.RuleStatEntity;
import com.bankingApp.offersToClients.repository.RuleRepository;
import com.bankingApp.offersToClients.repository.RuleStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collects rule hit statistics.
 *
 * <p>Counters are stored in {@code rule_stat}; rules that have never fired have no row there and
 * are reported with {@code count = 0} by {@link #getStats()}.</p>
 */
@Service
public class RuleStatService {

    private final RuleStatRepository ruleStatRepository;
    private final RuleRepository ruleRepository;

    public RuleStatService(RuleStatRepository ruleStatRepository, RuleRepository ruleRepository) {
        this.ruleStatRepository = ruleStatRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Increments the hit counter of a rule by one.
     *
     * <p>Implemented as a single {@code INSERT ... ON CONFLICT DO UPDATE} statement, so concurrent
     * recommendation requests cannot lose increments.</p>
     *
     * @param ruleId identifier of the rule that matched
     */
    public void registerHit(Long ruleId) {
        ruleStatRepository.increment(ruleId);
    }

    /**
     * Returns statistics for every existing rule, including rules with zero hits.
     *
     * @return statistics ordered by rule identifier
     */
    @Transactional(readOnly = true)
    public List<RuleStatDto> getStats() {
        Map<Long, Long> hits = ruleStatRepository.findAll().stream()
                .collect(Collectors.toMap(
                        stat -> stat.getRule().getId(),
                        RuleStatEntity::getCount
                ));

        return ruleRepository.findAllIds().stream()
                .map(ruleId -> new RuleStatDto(ruleId, hits.getOrDefault(ruleId, 0L)))
                .toList();
    }
}
