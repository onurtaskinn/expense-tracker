package com.expensetracker.expense_tracker.dto.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to update an existing expense")
public class UpdateExpenseRequest {

    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "10000.00", message = "Amount cannot exceed $10,000")
    @Schema(description = "Updated expense amount", example = "30.75")
    private BigDecimal amount;

    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    @Schema(description = "Updated expense description", example = "Dinner at Italian restaurant")
    private String description;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Schema(description = "Updated expense category", example = "Food")
    private String category;

    @PastOrPresent(message = "Date cannot be in the future")
    @Schema(description = "Updated expense date", example = "2024-12-15")
    private LocalDate date;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Updated additional notes", example = "Business dinner with potential client")
    private String notes;

    // Default constructor
    public UpdateExpenseRequest() {}

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

    /**
     * Check if any field is provided for update
     */
    public boolean hasAnyUpdate() {
        return amount != null || description != null || category != null || 
               date != null || notes != null;
    }

    @Override
    public String toString() {
        return "UpdateExpenseRequest{" +
                "amount=" + amount +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", notes='" + notes + '\'' +
                '}';
    }
}