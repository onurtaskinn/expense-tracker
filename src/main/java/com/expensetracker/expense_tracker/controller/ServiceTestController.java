package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.entity.Expense;
import com.expensetracker.expense_tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Service Testing", description = "Testing endpoints for expense service layer operations")
public class ServiceTestController {

    @Autowired
    private ExpenseService expenseService;

    // ==================== CREATE OPERATIONS ====================

    @PostMapping("/create-sample-data")
    @Operation(summary = "Create sample data via service", description = "Creates sample expenses using business logic")
    public ResponseEntity<String> createSampleData() {
        try {
            // Create sample expenses with business validation
            expenseService.createExpense(new BigDecimal("25.50"), "Lunch at McDonald's", "food", LocalDate.now());
            expenseService.createExpense(new BigDecimal("60.00"), "Weekly groceries", "Food", LocalDate.now());
            expenseService.createExpense(new BigDecimal("30.00"), "Gas station fill-up", "transportation", LocalDate.now());
            expenseService.createExpense(new BigDecimal("15.75"), "Coffee and pastry", "dining", LocalDate.now().minusDays(1));
            expenseService.createExpense(new BigDecimal("120.00"), "Monthly Metro pass", "Transport", LocalDate.now().minusDays(2));
            expenseService.createExpense(new BigDecimal("85.99"), "New headphones", "shopping", LocalDate.now().minusDays(3));

            return ResponseEntity.ok("✅ Sample data created successfully with business validation!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    @PostMapping("/create-expense")
    @Operation(summary = "Create single expense", description = "Create a new expense with validation")
    public ResponseEntity<?> createExpense(
            @Parameter(description = "Amount", example = "25.50") @RequestParam BigDecimal amount,
            @Parameter(description = "Description", example = "Lunch at restaurant") @RequestParam String description,
            @Parameter(description = "Category", example = "Food") @RequestParam String category,
            @Parameter(description = "Date (YYYY-MM-DD)", example = "2024-12-15") @RequestParam String date) {
        try {
            LocalDate expenseDate = LocalDate.parse(date);
            Expense expense = expenseService.createExpense(amount, description, category, expenseDate);
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== READ OPERATIONS ====================

    @GetMapping("/all")
    @Operation(summary = "Get all expenses (sorted)", description = "Retrieves all expenses sorted by business rules")
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/expense/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieves single expense with validation")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id) {
        try {
            Expense expense = expenseService.getExpenseById(id);
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get expenses by category", description = "Uses business logic for category normalization")
    public List<Expense> getByCategory(
            @Parameter(description = "Category (will be normalized)", example = "food") 
            @PathVariable String category) {
        return expenseService.getExpensesByCategory(category);
    }

    @GetMapping("/search")
    @Operation(summary = "Search expenses", description = "Search expenses by description with validation")
    public ResponseEntity<?> searchExpenses(
            @Parameter(description = "Search term", example = "coffee") 
            @RequestParam String term) {
        try {
            List<Expense> results = expenseService.searchExpenses(term);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== BUSINESS CALCULATIONS ====================

    @GetMapping("/total/category/{category}")
    @Operation(summary = "Calculate category total", description = "Business calculation for category spending")
    public ResponseEntity<String> getCategoryTotal(
            @Parameter(description = "Category name", example = "Food") 
            @PathVariable String category) {
        BigDecimal total = expenseService.calculateCategoryTotal(category);
        return ResponseEntity.ok(String.format("Total spent on %s: $%s", category, total));
    }

    @GetMapping("/total/current-month")
    @Operation(summary = "Current month total", description = "Calculate total spending for current month")
    public ResponseEntity<String> getCurrentMonthTotal() {
        BigDecimal total = expenseService.calculateCurrentMonthTotal();
        return ResponseEntity.ok(String.format("Current month total: $%s", total));
    }

    @GetMapping("/total/monthly/{year}/{month}")
    @Operation(summary = "Monthly total", description = "Calculate total for specific month")
    public ResponseEntity<String> getMonthlyTotal(
            @Parameter(description = "Year", example = "2024") @PathVariable int year,
            @Parameter(description = "Month (1-12)", example = "12") @PathVariable int month) {
        try {
            BigDecimal total = expenseService.calculateMonthlyTotal(year, month);
            return ResponseEntity.ok(String.format("Total for %d/%d: $%s", month, year, total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/summary/categories")
    @Operation(summary = "Category spending summary", description = "Get spending summary by category")
    public Map<String, BigDecimal> getCategorySpendingSummary() {
        return expenseService.getCategorySpendingSummary();
    }

    @GetMapping("/top-expensive")
    @Operation(summary = "Top expensive transactions", description = "Get top 5 most expensive transactions")
    public List<Expense> getTopExpensiveTransactions() {
        return expenseService.getTopExpensiveTransactions();
    }

    // ==================== UPDATE/DELETE OPERATIONS ====================

    @PutMapping("/expense/{id}")
    @Operation(summary = "Update expense", description = "Update expense with business validation")
    public ResponseEntity<?> updateExpense(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String date) {
        try {
            LocalDate expenseDate = LocalDate.parse(date);
            Expense updated = expenseService.updateExpense(id, amount, description, category, expenseDate);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/expense/{id}")
    @Operation(summary = "Delete expense", description = "Delete expense with business rules")
    public ResponseEntity<String> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok("✅ Expense deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== TESTING VALIDATION ====================

    @PostMapping("/test-validation")
    @Operation(summary = "Test validation rules", description = "Test various validation scenarios")
    public ResponseEntity<String> testValidation() {
        StringBuilder results = new StringBuilder("🧪 Validation Test Results:\n\n");

        // Test 1: Negative amount
        try {
            expenseService.createExpense(new BigDecimal("-10.00"), "Test", "Food", LocalDate.now());
            results.append("❌ FAIL: Negative amount should be rejected\n");
        } catch (Exception e) {
            results.append("✅ PASS: Negative amount rejected - ").append(e.getMessage()).append("\n");
        }

        // Test 2: Future date
        try {
            expenseService.createExpense(new BigDecimal("10.00"), "Test", "Food", LocalDate.now().plusDays(1));
            results.append("❌ FAIL: Future date should be rejected\n");
        } catch (Exception e) {
            results.append("✅ PASS: Future date rejected - ").append(e.getMessage()).append("\n");
        }

        // Test 3: Empty description
        try {
            expenseService.createExpense(new BigDecimal("10.00"), "", "Food", LocalDate.now());
            results.append("❌ FAIL: Empty description should be rejected\n");
        } catch (Exception e) {
            results.append("✅ PASS: Empty description rejected - ").append(e.getMessage()).append("\n");
        }

        // Test 4: Category normalization
        try {
            Expense expense = expenseService.createExpense(new BigDecimal("10.00"), "Test normalization", "food", LocalDate.now());
            if ("Food".equals(expense.getCategory())) {
                results.append("✅ PASS: Category normalized 'food' → 'Food'\n");
            } else {
                results.append("❌ FAIL: Category not normalized properly\n");
            }
        } catch (Exception e) {
            results.append("❌ FAIL: Category normalization error - ").append(e.getMessage()).append("\n");
        }

        return ResponseEntity.ok(results.toString());
    }
}