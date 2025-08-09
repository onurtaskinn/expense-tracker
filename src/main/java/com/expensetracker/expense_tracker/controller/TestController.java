package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.entity.Expense;
import com.expensetracker.expense_tracker.repository.ExpenseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/test")
@Tag(name = "Testing", description = "Testing endpoints for expense repository operations")
public class TestController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/create-sample-data")
    @Operation(summary = "Create sample data", description = "Creates 5 sample expenses for testing purposes")
    public String createSampleData() {
        // Create sample expenses
        expenseRepository.save(new Expense(new BigDecimal("25.50"), "Lunch at McDonald's", "Food", LocalDate.now()));
        expenseRepository.save(new Expense(new BigDecimal("60.00"), "Weekly groceries", "Food", LocalDate.now()));
        expenseRepository.save(new Expense(new BigDecimal("30.00"), "Gas station", "Transportation", LocalDate.now()));
        expenseRepository.save(new Expense(new BigDecimal("15.75"), "Coffee and pastry", "Food", LocalDate.of(2024, 12, 1)));
        expenseRepository.save(new Expense(new BigDecimal("120.00"), "Uber rides", "Transportation", LocalDate.of(2024, 11, 28)));

        return "✅ Sample data created! Check other endpoints to see the data.";
    }

    @GetMapping("/all")
    @Operation(summary = "Get all expenses", description = "Retrieves all expenses from the database")
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get expenses by category", description = "Retrieves all expenses for a specific category")
    public List<Expense> getByCategory(
            @Parameter(description = "Category name (e.g., Food, Transportation)", example = "Food")
            @PathVariable String category) {
        return expenseRepository.findByCategory(category);
    }

    @GetMapping("/expensive/{amount}")
    @Operation(summary = "Get expensive items", description = "Retrieves expenses greater than the specified amount")
    public List<Expense> getExpensive(
            @Parameter(description = "Minimum amount", example = "50.00")
            @PathVariable BigDecimal amount) {
        return expenseRepository.findByAmountGreaterThan(amount);
    }

    @GetMapping("/total/{category}")
    @Operation(summary = "Calculate category total", description = "Calculates total amount spent in a specific category")
    public String getTotalByCategory(
            @Parameter(description = "Category name", example = "Food")
            @PathVariable String category) {
        BigDecimal total = expenseRepository.calculateTotalByCategory(category);
        return "Total spent on " + category + ": $" + total;
    }

    /**
     * Search expenses by description
     * Test: GET http://localhost:8080/test/search/coffee
     */
    @GetMapping("/search/{term}")
    public List<Expense> searchByDescription(@PathVariable String term) {
        return expenseRepository.findByDescriptionContainingIgnoreCase(term);
    }

    /**
     * Get count of all expenses
     * Test: GET http://localhost:8080/test/count
     */
    @GetMapping("/count")
    public String getCount() {
        long count = expenseRepository.count();
        return "Total expenses in database: " + count;
    }

    /**
     * Delete all expenses (for cleanup)
     * Test: GET http://localhost:8080/test/clear
     */
    @GetMapping("/clear")
    public String clearAllData() {
        expenseRepository.deleteAll();
        return "🗑️ All expenses deleted!";
    }
}