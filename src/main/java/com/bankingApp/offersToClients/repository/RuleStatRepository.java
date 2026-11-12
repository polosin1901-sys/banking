package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.entity.RuleStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RuleStatRepository extends JpaRepository<RuleStatEntity, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO rule_stat (rule_id, hit_count)
            VALUES (:ruleId, 1)
            ON CONFLICT (rule_id)
            DO UPDATE SET hit_count = rule_stat.hit_count + 1
            """, nativeQuery = true)
    int increment(@Param("ruleId") Long ruleId);
}