package com.bankingApp.offersToClients.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Single query of a dynamic rule.
 *
 * <p>Arguments are stored as an ordered collection in {@code rule_query_argument}; the collection
 * order is significant because evaluators read arguments by index.</p>
 */
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

    /**
     * Zero-based position of the query inside its rule.
     *
     * <p>Hibernate 7 no longer writes the order column of an inverse collection, so the position
     * is assigned by {@code RuleEntity#addQuery} and read back through {@code @OrderBy}.</p>
     */
    @Column(name = "position", nullable = false)
    private int position;

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

    /**
     * Replaces the argument list.
     *
     * @param arguments new arguments; {@code null} clears the list
     */
    public void setArguments(List<String> arguments) {
        this.arguments.clear();
        if (arguments != null) {
            this.arguments.addAll(arguments);
        }
    }

    void setRule(RuleEntity rule) {
        this.rule = rule;
    }

    void setPosition(int position) {
        this.position = position;
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
