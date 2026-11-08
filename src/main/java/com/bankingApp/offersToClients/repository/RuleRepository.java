package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.entity.RuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RuleRepository extends JpaRepository<RuleEntity,Long> {

    @EntityGraph(attributePaths = {"rule", "rule.arguments"})
    @Query("select distinct r from RuleEntity r")
    List<RuleEntity> findAllWithQueries();
}
