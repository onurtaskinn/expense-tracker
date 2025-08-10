package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.response.ApiResponse;
import com.expensetracker.expense_tracker.dto.response.CategorySummaryResponse;
import com.expensetracker.expense_tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Basic Reports", description = "Simple reporting operations")
public class BasicReportsController {

    @Autowired
    private ExpenseService expenseService;

    // ==================== BASIC TOTALS ====================

    @GetMapping("/total")
    @Operation(summary = "Get total spending", description = "Returns total amount of all expenses")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalSpending() {
        CategorySummaryResponse summary = expenseService.getCategorySpendingSummary();
        BigDecimal total = summary.getTotalAmount();
        return ResponseEntity.ok(ApiResponse.success(total, "Total spending calculated"));
    }

    @GetMapping("/total/current-month")
    @Operation(summary = "Get current month total", description = "Returns total spending for current month")
    public ResponseEntity<ApiResponse<BigDecimal>> getCurrentMonthTotal() {
        BigDecimal total = expenseService.getCurrentMonthTotal();
        return ResponseEntity.ok(ApiResponse.success(total, "Current month total"));
    }

    @GetMapping("/total/month/{year}/{month}")
    @Operation(summary = "Get monthly total", description = "Returns total spending for specific month")
    public ResponseEntity<ApiResponse<BigDecimal>> getMonthlyTotal(
            @Parameter(description = "Year", example = "2024") @PathVariable int year,
            @Parameter(description = "Month (1-12)", example = "12") @PathVariable int month) {
        
        BigDecimal total = expenseService.getMonthlyTotal(year, month);
        return ResponseEntity.ok(ApiResponse.success(total, String.format("Total for %d/%d", month, year)));
    }

    @GetMapping("/total/category/{category}")
    @Operation(summary = "Get category total", description = "Returns total spending for specific category")
    public ResponseEntity<ApiResponse<BigDecimal>> getCategoryTotal(
            @Parameter(description = "Category name", example = "Food") @PathVariable String category) {
        
        // Get all expenses for category and calculate total
        var expenses = expenseService.getExpensesByCategory(category);
        BigDecimal total = expenses.getExpenses().stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return ResponseEntity.ok(ApiResponse.success(total, "Category total: " + category));
    }

    // ==================== BASIC COUNTS ====================

    @GetMapping("/count")
    @Operation(summary = "Get total expense count", description = "Returns total number of expenses")
    public ResponseEntity<ApiResponse<Long>> getTotalCount() {
        var allExpenses = expenseService.getAllExpenses(null);
        long count = allExpenses.getTotalElements();
        return ResponseEntity.ok(ApiResponse.success(count, "Total expense count"));
    }

    @GetMapping("/count/category/{category}")
    @Operation(summary = "Get category count", description = "Returns number of expenses in category")
    public ResponseEntity<ApiResponse<Long>> getCategoryCount(
            @Parameter(description = "Category name", example = "Food") @PathVariable String category) {
        
        var expenses = expenseService.getExpensesByCategory(category);
        long count = expenses.getTotalElements();
        return ResponseEntity.ok(ApiResponse.success(count, "Count for category: " + category));
    }

    // ==================== BASIC SUMMARY ====================

    @GetMapping("/categories")
    @Operation(summary = "Get category summary", description = "Returns spending breakdown by category")
    public ResponseEntity<ApiResponse<CategorySummaryResponse>> getCategorySummary() {
        CategorySummaryResponse summary = expenseService.getCategorySpendingSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Category summary"));
    }

    @GetMapping("/overview")
    @Operation(summary = "Get basic overview", description = "Returns basic statistics overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBasicOverview() {
        // Get basic stats
        CategorySummaryResponse categorySummary = expenseService.getCategorySpendingSummary();
        BigDecimal currentMonthTotal = expenseService.getCurrentMonthTotal();
        var allExpenses = expenseService.getAllExpenses(null);
        
        // Create simple overview using Map (no warnings)
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalSpending", categorySummary.getTotalAmount());
        overview.put("totalExpenses", allExpenses.getTotalElements());
        overview.put("totalCategories", categorySummary.getCategoryCount());
        overview.put("currentMonthSpending", currentMonthTotal);
        
        return ResponseEntity.ok(ApiResponse.success(overview, "Basic overview"));
    }    
}