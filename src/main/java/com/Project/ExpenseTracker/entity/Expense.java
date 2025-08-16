package com.Project.ExpenseTracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses",
        indexes = {
                @Index(name = "idx_expense_user_id", columnList = "user_id"),
                @Index(name = "idx_expense_category", columnList = "category"),
                @Index(name = "idx_expense_date", columnList = "date"),
                @Index(name = "idx_expense_amount", columnList = "amount"),
                @Index(name = "idx_expense_user_category", columnList = "user_id, category"),
                @Index(name = "idx_expense_user_date", columnList = "user_id, date")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 20)
    private String paymentMethod; // CASH, CARD, UPI, etc.

    @Column(length = 100)
    private String tags; // Comma-separated tags

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
