package com.expensetracker.expense_tracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "Category spending summary response")
public class CategorySummaryResponse {

    @Schema(description = "Spending by category")
    private List<CategorySpending> categories;

    @Schema(description = "Total amount across all categories", example = "1250.75")
    private BigDecimal totalAmount;

    @Schema(description = "Number of different categories", example = "5")
    private int categoryCount;

    @Schema(description = "Most expensive category")
    private CategorySpending topCategory;

    // Default constructor
    public CategorySummaryResponse() {}

    // Constructor
    public CategorySummaryResponse(List<CategorySpending> categories, BigDecimal totalAmount) {
        this.categories = categories;
        this.totalAmount = totalAmount;
        this.categoryCount = categories.size();
        this.topCategory = categories.stream()
                .max((c1, c2) -> c1.getAmount().compareTo(c2.getAmount()))
                .orElse(null);
    }

    // Static factory method from Map
    public static CategorySummaryResponse fromMap(Map<String, BigDecimal> categoryTotals) {
        List<CategorySpending> categories = categoryTotals.entrySet().stream()
                .map(entry -> new CategorySpending(entry.getKey(), entry.getValue()))
                .sorted((c1, c2) -> c2.getAmount().compareTo(c1.getAmount())) // Sort by amount desc
                .toList();

        BigDecimal total = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CategorySummaryResponse(categories, total);
    }

    // Inner class for category spending
    @Schema(description = "Category spending details")
    public static class CategorySpending {
        @Schema(description = "Category name", example = "Food")
        private String category;

        @Schema(description = "Total amount spent", example = "450.25")
        private BigDecimal amount;

        @Schema(description = "Percentage of total spending", example = "36.2")
        private Double percentage;

        @Schema(description = "Number of transactions", example = "15")
        private Integer transactionCount;

        public CategorySpending() {}

        public CategorySpending(String category, BigDecimal amount) {
            this.category = category;
            this.amount = amount;
        }

        public CategorySpending(String category, BigDecimal amount, Double percentage, Integer transactionCount) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
            this.transactionCount = transactionCount;
        }

        // Getters and Setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        public Integer getTransactionCount() {
            return transactionCount;
        }

        public void setTransactionCount(Integer transactionCount) {
            this.transactionCount = transactionCount;
        }

        @Override
        public String toString() {
            return "CategorySpending{" +
                    "category='" + category + '\'' +
                    ", amount=" + amount +
                    ", percentage=" + percentage +
                    ", transactionCount=" + transactionCount +
                    '}';
        }
    }

    // Method to calculate percentages
    public void calculatePercentages() {
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            categories.forEach(category -> {
                double percentage = category.getAmount()
                        .divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                category.setPercentage(percentage);
            });
        }
    }

    // Getters and Setters
    public List<CategorySpending> getCategories() {
        return categories;
    }

    public void setCategories(List<CategorySpending> categories) {
        this.categories = categories;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }

    public CategorySpending getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(CategorySpending topCategory) {
        this.topCategory = topCategory;
    }

    @Override
    public String toString() {
        return "CategorySummaryResponse{" +
                "categoryCount=" + categoryCount +
                ", totalAmount=" + totalAmount +
                ", topCategory=" + (topCategory != null ? topCategory.getCategory() : "none") +
                '}';
    }
}