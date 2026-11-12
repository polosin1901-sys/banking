package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RuleStatDto;
import com.bankingApp.offersToClients.entity.RuleStatEntity;
import com.bankingApp.offersToClients.repository.RuleRepository;
import com.bankingApp.offersToClients.repository.RuleStatRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RuleStatService {

    private final RuleStatRepository ruleStatRepository;
    private final RuleRepository ruleRepository;

    public RuleStatService(RuleStatRepository ruleStatRepository, RuleRepository ruleRepository) {
        this.ruleStatRepository = ruleStatRepository;
        this.ruleRepository = ruleRepository;
    }

    public void registerHit (Long id) {
        ruleStatRepository.increment(id);
    }

    @Transactional (readOnly = true)
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
