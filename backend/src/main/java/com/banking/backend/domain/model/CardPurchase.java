package com.banking.backend.domain.model;

import com.banking.backend.domain.enums.CardPurchaseStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_purchases")
public class CardPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "purchase_reference", nullable = false, unique = true, length = 80)
    private String purchaseReference;

    @Column(name = "merchant_name", nullable = false, length = 150)
    private String merchantName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardPurchaseStatus status;

    @Column(name = "refunded_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_transaction_id")
    private Transaction settlementTransaction;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CardPurchase() {
    }

    public CardPurchase(Card card, String purchaseReference, String merchantName,
                        BigDecimal amount, CardPurchaseStatus status) {
        this.card = card;
        this.purchaseReference = purchaseReference;
        this.merchantName = merchantName;
        this.amount = amount;
        this.status = status;
        this.refundedAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }

    public String getPurchaseReference() {
        return purchaseReference;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CardPurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(CardPurchaseStatus status) {
        this.status = status;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public Transaction getSettlementTransaction() {
        return settlementTransaction;
    }

    public void setSettlementTransaction(Transaction settlementTransaction) {
        this.settlementTransaction = settlementTransaction;
    }

    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }

    public void setAuthorizedAt(LocalDateTime authorizedAt) {
        this.authorizedAt = authorizedAt;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
