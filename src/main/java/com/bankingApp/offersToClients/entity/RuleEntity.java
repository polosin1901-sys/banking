package com.bankingApp.offersToClients.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dynamic recommendation rule created by the manager.
 *
 * <p>A rule fires when all queries of {@link #getRule()} match. Queries are stored in the
 * {@code rule_query} table and keep their original order through the {@code position} column.</p>
 */
@Entity
@Table(name = "recommendation_rule")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_text", nullable = false, columnDefinition = "text")
    private String productText;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QueryEntity> rule = new ArrayList<>();

    protected RuleEntity() {
    }

    /**
     * Creates a rule with its queries.
     *
     * @param productName product name shown to the client
     * @param productId   product identifier
     * @param productText marketing description
     * @param rule        queries that must all match
     */
    public RuleEntity(String productName, UUID productId, String productText,
                      List<QueryEntity> rule) {
        this.productName = productName;
        this.productId = productId;
        this.productText = productText;
        setRule(rule);
    }

    /**
     * Replaces the rule queries, keeping the bidirectional association consistent.
     *
     * @param rule new queries; {@code null} clears the rule
     */
    public void setRule(List<QueryEntity> rule) {
        this.rule.clear();
        if (rule != null) {
            rule.forEach(this::addQuery);
        }
    }

    /**
     * Appends a query and links it back to this rule.
     *
     * <p>The position is calculated from the current list size, so the stored order matches the
     * order in which the manager added the queries.</p>
     *
     * @param query query to append
     */
    public void addQuery(QueryEntity query) {
        query.setRule(this);
        query.setPosition(this.rule.size());
        this.rule.add(query);
    }

    /**
     * @return generated rule identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * @return product name shown to the client
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return product identifier used for de-duplication
     */
    public UUID getProductId() {
        return productId;
    }

    /**
     * @return marketing description of the product
     */
    public String getProductText() {
        return productText;
    }

    /**
     * @return queries in the order in which they were added
     */
    public List<QueryEntity> getRule() {
        return rule;
    }
}
