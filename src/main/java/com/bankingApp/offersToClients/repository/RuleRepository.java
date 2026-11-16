package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.entity.RuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * JPA repository of dynamic recommendation rules.
 */
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {

    /**
     * Loads all rules together with their queries and query arguments in a single query graph.
     *
     * @return all rules with fetched queries
     */
    @EntityGraph(attributePaths = {"rule", "rule.arguments"})
    @Query("select distinct r from RuleEntity r")
    List<RuleEntity> findAllWithQueries();

    /**
     * @return identifiers of all rules ordered ascending
     */
    @Query("select r.id from RuleEntity r order by r.id")
    List<Long> findAllIds();
}
