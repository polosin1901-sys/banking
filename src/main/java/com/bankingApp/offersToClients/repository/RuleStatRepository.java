package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.entity.RuleStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository of rule hit counters.
 */
public interface RuleStatRepository extends JpaRepository<RuleStatEntity, Long> {

    /**
     * Atomically increments the hit counter of a rule.
     *
     * <p>The statement is a PostgreSQL upsert: the first hit inserts a row with {@code 1}, later
     * hits update the existing row. Because the whole operation happens inside the database, two
     * parallel requests cannot overwrite each other's increment. The method must be called from a
     * transaction, which is provided by {@code RuleStatService.registerHit} callers.</p>
     *
     * @param ruleId rule identifier
     * @return number of affected rows
     */
    @Modifying
    @Query(value = """
            INSERT INTO rule_stat (rule_id, hit_count)
            VALUES (:ruleId, 1)
            ON CONFLICT (rule_id)
            DO UPDATE SET hit_count = rule_stat.hit_count + 1
            """, nativeQuery = true)
    int increment(@Param("ruleId") Long ruleId);
}
