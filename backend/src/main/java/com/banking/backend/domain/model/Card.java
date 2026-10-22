package com.banking.backend.domain.model;

import com.banking.backend.domain.enums.CardStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "card_reference", nullable = false, unique = true, length = 50)
    private String cardReference;

    @JdbcTypeCode(Types.CHAR)
    @Column(length = 4)
    private String lastFour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "card")
    private List<CardPurchase> purchases = new ArrayList<>();

    public Card() {
    }

    public Card(Account account, Customer customer, String cardReference,
                LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.account = account;
        this.customer = customer;
        this.cardReference = cardReference;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.status = CardStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCardReference() {
        return cardReference;
    }

    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public List<CardPurchase> getPurchases() {
        return purchases;
    }
}
