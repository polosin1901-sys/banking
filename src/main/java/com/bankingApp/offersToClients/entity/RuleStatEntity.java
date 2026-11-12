package com.bankingApp.offersToClients.entity;

import jakarta.persistence.*;

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
