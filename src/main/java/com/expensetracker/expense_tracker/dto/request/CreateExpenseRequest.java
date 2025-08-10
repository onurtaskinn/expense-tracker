package com.expensetracker.expense_tracker.dto.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to create a new expense")
public class CreateExpenseRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "10000.00", message = "Amount cannot exceed $10,000")
    @Schema(description = "Expense amount", example = "25.50", required = true)
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    @Schema(description = "Expense description", example = "Lunch at McDonald's", required = true)
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Schema(description = "Expense category", example = "Food", required = true)
    private String category;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    @Schema(description = "Expense date", example = "2024-12-15", required = true)
    private LocalDate date;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes (optional)", example = "Business lunch with client")
    private String notes;

    // Default constructor
    public CreateExpenseRequest() {}

    // Constructor for easy testing
    public CreateExpenseRequest(BigDecimal amount, String description, String category, LocalDate date) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }

    // Full constructor
    public CreateExpenseRequest(BigDecimal amount, String description, String category, LocalDate date, String notes) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.notes = notes;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "CreateExpenseRequest{" +
                "amount=" + amount +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", notes='" + notes + '\'' +
                '}';
    }
}