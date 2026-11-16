package com.bankingApp.offersToClients.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Hit counter of a dynamic rule.
 *
 * <p>The database keeps at most one row per rule ({@code rule_id} is unique) and removes the row
 * together with the rule because of {@code ON DELETE CASCADE}. Counters are incremented with a
 * native upsert, not through this entity.</p>
 */
@Entity
@Table(name = "rule_stat")
public class RuleStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false, unique = true)
    private RuleEntity rule;

    @Column(name = "hit_count", nullable = false)
    private long count;

    public RuleStatEntity(Long id, RuleEntity rule, long count) {
        this.id = id;
        this.rule = rule;
        this.count = count;
    }

    public RuleStatEntity() {
    }

    public Long getId() {
        return id;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public long getCount() {
        return count;
    }
}
