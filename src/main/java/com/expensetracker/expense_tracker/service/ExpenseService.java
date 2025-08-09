package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.entity.Expense;
import com.expensetracker.expense_tracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    // ==================== CORE CRUD OPERATIONS ====================

    /**
     * Create a new expense with business validation
     */
    public Expense createExpense(BigDecimal amount, String description, String category, LocalDate date) {
        // Business validation
        validateExpenseData(amount, description, category, date);
        
        // Apply business rules
        String normalizedCategory = normalizeCategory(category);
        String cleanDescription = cleanDescription(description);
        
        // Check spending limits (business rule)
        checkMonthlySpendingLimit(normalizedCategory, amount, date);
        
        // Create and save expense
        Expense expense = new Expense(amount, cleanDescription, normalizedCategory, date);
        
        return expenseRepository.save(expense);
    }

    /**
     * Get expense by ID with proper error handling
     */
    public Expense getExpenseById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid expense ID: " + id);
        }
        
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + id));
    }

    /**
     * Get all expenses sorted by date (newest first)
     */
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        
        // Business logic: Sort by date descending, then by amount descending
        return expenses.stream()
                .sorted((e1, e2) -> {
                    int dateCompare = e2.getDate().compareTo(e1.getDate());
                    if (dateCompare == 0) {
                        return e2.getAmount().compareTo(e1.getAmount());
                    }
                    return dateCompare;
                })
                .collect(Collectors.toList());
    }

    /**
     * Update an existing expense
     */
    public Expense updateExpense(Long id, BigDecimal amount, String description, String category, LocalDate date) {
        // Check if expense exists
        Expense existingExpense = getExpenseById(id);
        
        // Validate new data
        validateExpenseData(amount, description, category, date);
        
        // Apply business rules
        String normalizedCategory = normalizeCategory(category);
        String cleanDescription = cleanDescription(description);
        
        // Update fields
        existingExpense.setAmount(amount);
        existingExpense.setDescription(cleanDescription);
        existingExpense.setCategory(normalizedCategory);
        existingExpense.setDate(date);
        
        return expenseRepository.save(existingExpense);
    }

    /**
     * Delete an expense with business rules
     */
    public void deleteExpense(Long id) {
        // Check if expense exists
        Expense expense = getExpenseById(id);
        
        // Business rule: Can't delete expenses older than 1 year
        if (expense.getDate().isBefore(LocalDate.now().minusYears(1))) {
            throw new RuntimeException("Cannot delete expenses older than 1 year for audit purposes");
        }
        
        expenseRepository.deleteById(id);
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Calculate total expenses for a specific category
     */
    public BigDecimal calculateCategoryTotal(String category) {
        String normalizedCategory = normalizeCategory(category);
        BigDecimal total = expenseRepository.calculateTotalByCategory(normalizedCategory);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total expenses for current month
     */
    public BigDecimal calculateCurrentMonthTotal() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        List<Expense> monthlyExpenses = expenseRepository.findByDateBetween(startDate, endDate);
        
        return monthlyExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total expenses for specific month and year
     */
    public BigDecimal calculateMonthlyTotal(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Expense> monthlyExpenses = expenseRepository.findByDateBetween(startDate, endDate);
        
        return monthlyExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get expenses by category with business logic
     */
    public List<Expense> getExpensesByCategory(String category) {
        String normalizedCategory = normalizeCategory(category);
        List<Expense> expenses = expenseRepository.findByCategoryOrderByDateDesc(normalizedCategory);
        
        // Business logic: Log frequently accessed categories
        if (expenses.size() > 10) {
            System.out.println("Category '" + normalizedCategory + "' is frequently used (" + expenses.size() + " expenses)");
        }
        
        return expenses;
    }

    /**
     * Get category spending summary
     */
    public Map<String, BigDecimal> getCategorySpendingSummary() {
        List<Expense> allExpenses = expenseRepository.findAll();
        
        return allExpenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, 
                                      Expense::getAmount, 
                                      BigDecimal::add)
                ));
    }

    /**
     * Get top 5 most expensive transactions
     */
    public List<Expense> getTopExpensiveTransactions() {
        return expenseRepository.findAll().stream()
                .sorted((e1, e2) -> e2.getAmount().compareTo(e1.getAmount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Search expenses by description
     */
    public List<Expense> searchExpenses(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }
        
        return expenseRepository.findByDescriptionContainingIgnoreCase(searchTerm.trim());
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validate expense data according to business rules
     */
    private void validateExpenseData(BigDecimal amount, String description, String category, LocalDate date) {
        // Amount validation
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Amount cannot exceed $10,000 per transaction. Got: " + amount);
        }

        // Description validation
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (description.length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }

        // Category validation
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }

        // Date validation
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create future-dated expenses");
        }
        if (date.isBefore(LocalDate.now().minusYears(2))) {
            throw new IllegalArgumentException("Cannot create expenses older than 2 years");
        }
    }

    /**
     * Check monthly spending limits (business rule)
     */
    private void checkMonthlySpendingLimit(String category, BigDecimal newAmount, LocalDate date) {
        // Define spending limits per category
        Map<String, BigDecimal> monthlyLimits = Map.of(
            "Food", new BigDecimal("1000"),
            "Transportation", new BigDecimal("500"),
            "Entertainment", new BigDecimal("300"),
            "Shopping", new BigDecimal("800")
        );

        BigDecimal limit = monthlyLimits.get(category);
        if (limit != null) {
            YearMonth yearMonth = YearMonth.from(date);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            
            List<Expense> monthlyExpenses = expenseRepository.findByDateBetween(startDate, endDate)
                    .stream()
                    .filter(expense -> expense.getCategory().equals(category))
                    .collect(Collectors.toList());
                    
            BigDecimal currentTotal = monthlyExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            BigDecimal newTotal = currentTotal.add(newAmount);
            
            if (newTotal.compareTo(limit) > 0) {
                throw new RuntimeException(String.format(
                    "Monthly spending limit exceeded for %s. Limit: $%s, Current: $%s, New expense: $%s",
                    category, limit, currentTotal, newAmount));
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Normalize category names (business rule)
     */
    private String normalizeCategory(String category) {
        if (category == null) return "Other";
        
        String normalized = category.trim();
        
        // Convert to Title Case
        normalized = normalized.substring(0, 1).toUpperCase() + 
                    normalized.substring(1).toLowerCase();
        
        // Map common variations to standard categories
        switch (normalized.toLowerCase()) {
            case "food", "dining", "restaurant", "groceries":
                return "Food";
            case "transport", "transportation", "gas", "fuel", "uber", "taxi":
                return "Transportation";
            case "fun", "entertainment", "movies", "games":
                return "Entertainment";
            case "clothes", "shopping", "retail":
                return "Shopping";
            case "medical", "health", "doctor", "pharmacy":
                return "Healthcare";
            default:
                return normalized;
        }
    }

    /**
     * Clean and format description
     */
    private String cleanDescription(String description) {
        if (description == null) return "";
        
        return description.trim()
                .replaceAll("\\s+", " ") // Remove extra spaces
                .substring(0, Math.min(description.trim().length(), 255)); // Ensure max length
    }
}