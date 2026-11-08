package com.bankingApp.offersToClients.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rule_query")
public class QueryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleEntity rule;

    @Column(name = "query_type", nullable = false, length = 64)
    private String query;

    @Column(name = "negate", nullable = false)
    private boolean negate;

    @ElementCollection
    @CollectionTable(
            name = "rule_query_argument",
            joinColumns = @JoinColumn(name = "query_id")
    )
    @OrderColumn(name = "position")
    @Column(name = "argument", nullable = false)
    private List<String> arguments = new ArrayList<>();

    protected QueryEntity() {
    }

    public QueryEntity(String query, List<String> arguments, boolean negate) {
        this.query = query;
        this.negate = negate;
        setArguments(arguments);
    }

    public void setArguments(List<String> arguments) {
        this.arguments.clear();
        if (arguments != null) {
            this.arguments.addAll(arguments);
        }
    }

    void setRule(RuleEntity rule) {
        this.rule = rule;
    }

    public Long getId() {
        return id;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public String getQuery() {
        return query;
    }

    public boolean isNegate() {
        return negate;
    }

    public List<String> getArguments() {
        return arguments;
    }
}
