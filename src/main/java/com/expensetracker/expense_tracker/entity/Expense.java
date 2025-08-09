package com.expensetracker.expense_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotBlank(message = "Description is required")
    @Column(nullable = false)
    private String description;
    
    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;
    
    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    // Default constructor (required by JPA)
    public Expense() {}
    
    // Constructor for easy object creation
    public Expense(BigDecimal amount, String description, String category, LocalDate date) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.createdAt = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
    
    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", createdAt=" + createdAt +
                '}';
    }
}