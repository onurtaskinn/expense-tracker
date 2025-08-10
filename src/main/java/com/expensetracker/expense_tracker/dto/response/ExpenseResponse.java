package com.expensetracker.expense_tracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.expensetracker.expense_tracker.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Expense response data")
public class ExpenseResponse {

    @Schema(description = "Unique expense identifier", example = "1")
    private Long id;

    @Schema(description = "Expense amount", example = "25.50")
    private BigDecimal amount;

    @Schema(description = "Expense description", example = "Lunch at McDonald's")
    private String description;

    @Schema(description = "Expense category", example = "Food")
    private String category;

    @Schema(description = "Expense date", example = "2024-12-15")
    private LocalDate date;

    @Schema(description = "Additional notes", example = "Business lunch with client")
    private String notes;

    @Schema(description = "Date when expense was created", example = "2024-12-15")
    private LocalDate createdAt;

    // Default constructor
    public ExpenseResponse() {}

    // Constructor from Entity
    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.amount = expense.getAmount();
        this.description = expense.getDescription();
        this.category = expense.getCategory();
        this.date = expense.getDate();
        this.createdAt = expense.getCreatedAt();
        // Note: We don't expose notes field from entity as it doesn't exist yet
        this.notes = null; // Will be added when we enhance the entity
    }

    // Full constructor
    public ExpenseResponse(Long id, BigDecimal amount, String description, String category, 
                          LocalDate date, String notes, LocalDate createdAt) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Static factory method for easy conversion
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(expense);
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ExpenseResponse{" +
                "id=" + id +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}