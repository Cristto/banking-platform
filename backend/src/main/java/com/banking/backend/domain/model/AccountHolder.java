package com.banking.backend.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_holders",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_account_holders_account_customer",
           columnNames = {"account_id", "customer_id"}
       ))
public class AccountHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    public AccountHolder() {
    }

    public AccountHolder(Account account, Customer customer, String role) {
        this.account = account;
        this.customer = customer;
        this.role = role;
        this.addedAt = LocalDateTime.now();
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

    public String getRole() {
        return role;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
