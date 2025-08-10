package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.mapper.ExpenseDTOMapper;
import com.expensetracker.expense_tracker.dto.request.CreateExpenseRequest;
import com.expensetracker.expense_tracker.dto.request.UpdateExpenseRequest;
import com.expensetracker.expense_tracker.dto.request.ExpenseSearchRequest;
import com.expensetracker.expense_tracker.dto.response.ExpenseResponse;
import com.expensetracker.expense_tracker.dto.response.ExpenseListResponse;
import com.expensetracker.expense_tracker.dto.response.CategorySummaryResponse;
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

    @Autowired
    private ExpenseDTOMapper dtoMapper;

    // ==================== CORE CRUD OPERATIONS WITH DTOS ====================

    /**
     * Create a new expense using DTO
     */
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        // Validate the request (additional validation can be added here)
        validateCreateRequest(request);
        
        // Convert DTO to Entity with normalization
        Expense expense = dtoMapper.toEntityWithNormalization(request);
        
        // Apply business rules
        checkMonthlySpendingLimit(expense.getCategory(), expense.getAmount(), expense.getDate());
        
        // Save and return response
        Expense savedExpense = expenseRepository.save(expense);
        return dtoMapper.toResponse(savedExpense);
    }

    /**
     * Get expense by ID
     */
    public ExpenseResponse getExpenseById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid expense ID: " + id);
        }
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + id));
                
        return dtoMapper.toResponse(expense);
    }

    /**
     * Get all expenses with optional search/filter
     */
    public ExpenseListResponse getAllExpenses(ExpenseSearchRequest searchRequest) {
        if (searchRequest == null) {
            // Simple case - get all expenses
            List<Expense> expenses = expenseRepository.findAll();
            expenses = sortExpensesByBusinessRules(expenses);
            return dtoMapper.toListResponse(expenses);
        }

        // Apply search/filter logic
        List<Expense> filteredExpenses = filterExpenses(searchRequest);
        
        // For now, return simple list response (pagination will be enhanced later)
        return dtoMapper.toListResponse(filteredExpenses);
    }

    /**
     * Update an existing expense
     */
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        // Validate the request
        if (!dtoMapper.hasUpdates(request)) {
            throw new IllegalArgumentException("No fields provided for update");
        }
        
        // Get existing expense
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + id));
        
        // Store original values for business rule checking
        String originalCategory = existingExpense.getCategory();
        BigDecimal originalAmount = existingExpense.getAmount();
        LocalDate originalDate = existingExpense.getDate();
        
        // Update the entity
        dtoMapper.updateEntity(existingExpense, request);
        
        // Apply normalization if category was updated
        if (request.getCategory() != null) {
            existingExpense.setCategory(normalizeCategory(request.getCategory()));
        }
        
        // Clean description if updated
        if (request.getDescription() != null) {
            existingExpense.setDescription(cleanDescription(request.getDescription()));
        }
        
        // Check business rules if relevant fields changed
        if (categoryOrAmountOrDateChanged(request, originalCategory, originalAmount, originalDate)) {
            checkMonthlySpendingLimit(existingExpense.getCategory(), 
                                    existingExpense.getAmount(), 
                                    existingExpense.getDate());
        }
        
        // Save and return
        Expense updatedExpense = expenseRepository.save(existingExpense);
        return dtoMapper.toResponse(updatedExpense);
    }

    /**
     * Delete an expense
     */
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + id));
        
        // Business rule: Can't delete expenses older than 1 year
        if (expense.getDate().isBefore(LocalDate.now().minusYears(1))) {
            throw new RuntimeException("Cannot delete expenses older than 1 year for audit purposes");
        }
        
        expenseRepository.deleteById(id);
    }

    // ==================== BUSINESS ANALYTICS WITH DTOS ====================

    /**
     * Get category spending summary
     */
    public CategorySummaryResponse getCategorySpendingSummary() {
        Map<String, BigDecimal> categoryTotals = expenseRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, 
                                      Expense::getAmount, 
                                      BigDecimal::add)
                ));

        CategorySummaryResponse response = CategorySummaryResponse.fromMap(categoryTotals);
        response.calculatePercentages();
        return response;
    }

    /**
     * Get monthly total spending
     */
    public BigDecimal getMonthlyTotal(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Expense> monthlyExpenses = expenseRepository.findByDateBetween(startDate, endDate);
        
        return monthlyExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get current month total spending
     */
    public BigDecimal getCurrentMonthTotal() {
        YearMonth currentMonth = YearMonth.now();
        return getMonthlyTotal(currentMonth.getYear(), currentMonth.getMonthValue());
    }

    /**
     * Get expenses by category
     */
    public ExpenseListResponse getExpensesByCategory(String category) {
        String normalizedCategory = normalizeCategory(category);
        List<Expense> expenses = expenseRepository.findByCategoryOrderByDateDesc(normalizedCategory);
        
        // Business logic: Log frequently accessed categories
        if (expenses.size() > 10) {
            System.out.println("Category '" + normalizedCategory + "' is frequently used (" + expenses.size() + " expenses)");
        }
        
        return dtoMapper.toListResponse(expenses);
    }

    /**
     * Search expenses
     */
    public ExpenseListResponse searchExpenses(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }
        
        List<Expense> expenses = expenseRepository.findByDescriptionContainingIgnoreCase(searchTerm.trim());
        return dtoMapper.toListResponse(expenses);
    }

    /**
     * Get top expensive transactions
     */
    public ExpenseListResponse getTopExpensiveTransactions(int limit) {
        List<Expense> expenses = expenseRepository.findAll().stream()
                .sorted((e1, e2) -> e2.getAmount().compareTo(e1.getAmount()))
                .limit(limit)
                .collect(Collectors.toList());
                
        return dtoMapper.toListResponse(expenses);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate create request
     */
    private void validateCreateRequest(CreateExpenseRequest request) {
        // Additional business validation beyond bean validation
        if (request.getAmount() != null && request.getAmount().scale() > 2) {
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }
        
        // Check for business-specific rules
        if (request.getDescription() != null && 
            request.getDescription().toLowerCase().contains("test") && 
            request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            throw new IllegalArgumentException("Test expenses cannot exceed $1000");
        }
    }

    /**
     * Filter expenses based on search request
     */
    private List<Expense> filterExpenses(ExpenseSearchRequest searchRequest) {
        List<Expense> expenses = expenseRepository.findAll();
        
        // Apply filters
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            expenses = expenses.stream()
                    .filter(expense -> expense.getDescription()
                            .toLowerCase()
                            .contains(searchRequest.getSearchTerm().toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getCategory() != null) {
            String normalizedCategory = normalizeCategory(searchRequest.getCategory());
            expenses = expenses.stream()
                    .filter(expense -> expense.getCategory().equals(normalizedCategory))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getMinAmount() != null) {
            expenses = expenses.stream()
                    .filter(expense -> expense.getAmount().compareTo(searchRequest.getMinAmount()) >= 0)
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getMaxAmount() != null) {
            expenses = expenses.stream()
                    .filter(expense -> expense.getAmount().compareTo(searchRequest.getMaxAmount()) <= 0)
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getStartDate() != null) {
            expenses = expenses.stream()
                    .filter(expense -> !expense.getDate().isBefore(searchRequest.getStartDate()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getEndDate() != null) {
            expenses = expenses.stream()
                    .filter(expense -> !expense.getDate().isAfter(searchRequest.getEndDate()))
                    .collect(Collectors.toList());
        }
        
        // Apply sorting
        expenses = sortExpenses(expenses, searchRequest.getSortBy(), searchRequest.getSortDirection());
        
        return expenses;
    }

    /**
     * Sort expenses by business rules
     */
    private List<Expense> sortExpensesByBusinessRules(List<Expense> expenses) {
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
     * Sort expenses based on criteria
     */
    private List<Expense> sortExpenses(List<Expense> expenses, String sortBy, String sortDirection) {
        boolean ascending = "asc".equalsIgnoreCase(sortDirection);
        
        return expenses.stream()
                .sorted((e1, e2) -> {
                    int result = 0;
                    switch (sortBy.toLowerCase()) {
                        case "amount":
                            result = e1.getAmount().compareTo(e2.getAmount());
                            break;
                        case "description":
                            result = e1.getDescription().compareToIgnoreCase(e2.getDescription());
                            break;
                        case "category":
                            result = e1.getCategory().compareToIgnoreCase(e2.getCategory());
                            break;
                        case "createdat":
                            result = e1.getCreatedAt().compareTo(e2.getCreatedAt());
                            break;
                        case "date":
                        default:
                            result = e1.getDate().compareTo(e2.getDate());
                            break;
                    }
                    return ascending ? result : -result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if fields that affect business rules have changed
     */
    private boolean categoryOrAmountOrDateChanged(UpdateExpenseRequest request, 
                                                  String originalCategory, 
                                                  BigDecimal originalAmount, 
                                                  LocalDate originalDate) {
        return (request.getCategory() != null && !request.getCategory().equals(originalCategory)) ||
               (request.getAmount() != null && !request.getAmount().equals(originalAmount)) ||
               (request.getDate() != null && !request.getDate().equals(originalDate));
    }

    // ==================== BUSINESS RULE METHODS ====================
    // (Same as before, but kept for completeness)

    private void checkMonthlySpendingLimit(String category, BigDecimal newAmount, LocalDate date) {
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

    private String normalizeCategory(String category) {
        if (category == null) return "Other";
        
        String normalized = category.trim();
        normalized = normalized.substring(0, 1).toUpperCase() + 
                    normalized.substring(1).toLowerCase();
        
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

    private String cleanDescription(String description) {
        if (description == null) return "";
        
        return description.trim()
                .replaceAll("\\s+", " ")
                .substring(0, Math.min(description.trim().length(), 255));
    }
}