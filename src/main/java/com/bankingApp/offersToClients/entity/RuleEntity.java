package com.bankingApp.offersToClients.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL,orphanRemoval = true)
    @OrderColumn(name = "position")
    private List<QueryEntity> rule = new ArrayList<>();

    protected RuleEntity() {
    }

    public RuleEntity(String productName, UUID productId, String productText,
                      List<QueryEntity> rule) {
        this.productName = productName;
        this.productId = productId;
        this.productText = productText;
        setRule(rule);
    }

    public void setRule(List<QueryEntity> rule) {
        this.rule.clear();
        if (rule != null) {
            rule.forEach(this::addQuery);
        }
    }

    public void addQuery(QueryEntity query) {
        query.setRule(this);
        this.rule.add(query);
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductText() {
        return productText;
    }

    public List<QueryEntity> getRule() {
        return rule;
    }


}
