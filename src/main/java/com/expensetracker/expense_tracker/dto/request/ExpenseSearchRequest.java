package com.expensetracker.expense_tracker.dto.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request to search and filter expenses")
public class ExpenseSearchRequest {

    @Schema(description = "Search term for description", example = "lunch")
    private String searchTerm;

    @Schema(description = "Filter by category", example = "Food")
    private String category;

    @Schema(description = "Filter by multiple categories", example = "[\"Food\", \"Transportation\"]")
    private List<String> categories;

    @Schema(description = "Minimum amount", example = "10.00")
    @PositiveOrZero(message = "Minimum amount must be positive or zero")
    private BigDecimal minAmount;

    @Schema(description = "Maximum amount", example = "100.00")
    @Positive(message = "Maximum amount must be positive")
    private BigDecimal maxAmount;

    @Schema(description = "Start date for date range", example = "2024-12-01")
    private LocalDate startDate;

    @Schema(description = "End date for date range", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Page number (0-based)", example = "0")
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0;

    @Schema(description = "Page size", example = "20")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "date", allowableValues = {"date", "amount", "description", "category", "createdAt"})
    private String sortBy = "date";

    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    private String sortDirection = "desc";

    // Default constructor
    public ExpenseSearchRequest() {}

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * Validate date range
     */
    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        if (startDate != null && endDate != null) {
            return !endDate.isBefore(startDate);
        }
        return true;
    }

    /**
     * Validate amount range
     */
    @AssertTrue(message = "Maximum amount must be greater than minimum amount")
    public boolean isValidAmountRange() {
        if (minAmount != null && maxAmount != null) {
            return maxAmount.compareTo(minAmount) >= 0;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExpenseSearchRequest{" +
                "searchTerm='" + searchTerm + '\'' +
                ", category='" + category + '\'' +
                ", categories=" + categories +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}