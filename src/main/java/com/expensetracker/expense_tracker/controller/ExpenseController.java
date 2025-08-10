package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.request.CreateExpenseRequest;
import com.expensetracker.expense_tracker.dto.request.UpdateExpenseRequest;
import com.expensetracker.expense_tracker.dto.request.ExpenseSearchRequest;
import com.expensetracker.expense_tracker.dto.response.ApiResponse;
import com.expensetracker.expense_tracker.dto.response.ExpenseResponse;
import com.expensetracker.expense_tracker.dto.response.ExpenseListResponse;
import com.expensetracker.expense_tracker.dto.response.CategorySummaryResponse;
import com.expensetracker.expense_tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Expense management operations")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // ==================== CORE CRUD OPERATIONS ====================

    @PostMapping
    @Operation(
        summary = "Create a new expense", 
        description = "Creates a new expense with automatic validation and business rule enforcement"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Expense created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Business rule violation (e.g., spending limit exceeded)")
    })
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {
        
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Expense created successfully"));
    }

    @GetMapping
    @Operation(
        summary = "Get all expenses", 
        description = "Retrieves all expenses with optional filtering, sorting, and pagination"
    )
    public ResponseEntity<ApiResponse<ExpenseListResponse>> getAllExpenses(
            @Parameter(description = "Search term for description") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        // Build search request from query parameters
        ExpenseSearchRequest searchRequest = new ExpenseSearchRequest();
        searchRequest.setSearchTerm(search);
        searchRequest.setCategory(category);
        searchRequest.setMinAmount(minAmount);
        searchRequest.setMaxAmount(maxAmount);
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        
        ExpenseListResponse response = expenseService.getAllExpenses(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get expense by ID", 
        description = "Retrieves a specific expense by its unique identifier"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @Parameter(description = "Expense ID", example = "1") @PathVariable Long id) {
        
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update an expense", 
        description = "Updates an existing expense with validation and business rule enforcement"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @Parameter(description = "Expense ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request) {
        
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Expense updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an expense", 
        description = "Deletes an expense with business rule validation (e.g., audit restrictions)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot delete expense (business rule violation)")
    })
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @Parameter(description = "Expense ID", example = "1") @PathVariable Long id) {
        
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully"));
    }

    // ==================== SEARCH OPERATIONS ====================

    @PostMapping("/search")
    @Operation(
        summary = "Advanced expense search", 
        description = "Search expenses with complex filtering criteria using request body"
    )
    public ResponseEntity<ApiResponse<ExpenseListResponse>> searchExpenses(
            @Valid @RequestBody ExpenseSearchRequest searchRequest) {
        
        ExpenseListResponse response = expenseService.getAllExpenses(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Search completed successfully"));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Simple text search", 
        description = "Search expenses by description text"
    )
    public ResponseEntity<ApiResponse<ExpenseListResponse>> searchByText(
            @Parameter(description = "Search term", example = "lunch") @RequestParam String q) {
        
        ExpenseListResponse response = expenseService.searchExpenses(q);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get expenses by category", 
        description = "Retrieves all expenses for a specific category (with automatic normalization)"
    )
    public ResponseEntity<ApiResponse<ExpenseListResponse>> getExpensesByCategory(
            @Parameter(description = "Category name (will be normalized)", example = "food") 
            @PathVariable String category) {
        
        ExpenseListResponse response = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== ANALYTICS & REPORTS ====================

    @GetMapping("/analytics/summary")
    @Operation(
        summary = "Get spending summary by category", 
        description = "Returns detailed spending breakdown by category with percentages"
    )
    public ResponseEntity<ApiResponse<CategorySummaryResponse>> getSpendingSummary() {
        CategorySummaryResponse response = expenseService.getCategorySpendingSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/monthly-total")
    @Operation(
        summary = "Get current month total", 
        description = "Returns total spending for the current month"
    )
    public ResponseEntity<ApiResponse<BigDecimal>> getCurrentMonthTotal() {
        BigDecimal total = expenseService.getCurrentMonthTotal();
        return ResponseEntity.ok(ApiResponse.success(total, "Current month total calculated"));
    }

    @GetMapping("/analytics/monthly-total/{year}/{month}")
    @Operation(
        summary = "Get monthly total for specific period", 
        description = "Returns total spending for a specific month and year"
    )
    public ResponseEntity<ApiResponse<BigDecimal>> getMonthlyTotal(
            @Parameter(description = "Year", example = "2024") @PathVariable int year,
            @Parameter(description = "Month (1-12)", example = "12") @PathVariable int month) {
        
        BigDecimal total = expenseService.getMonthlyTotal(year, month);
        String message = String.format("Total for %d/%d: $%s", month, year, total);
        return ResponseEntity.ok(ApiResponse.success(total, message));
    }

    @GetMapping("/analytics/top-expenses")
    @Operation(
        summary = "Get top expensive transactions", 
        description = "Returns the most expensive transactions, limited by count"
    )
    public ResponseEntity<ApiResponse<ExpenseListResponse>> getTopExpenses(
            @Parameter(description = "Number of transactions to return", example = "10") 
            @RequestParam(defaultValue = "10") int limit) {
        
        ExpenseListResponse response = expenseService.getTopExpensiveTransactions(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== UTILITY ENDPOINTS ====================

    @GetMapping("/health")
    @Operation(
        summary = "Health check", 
        description = "Simple health check endpoint to verify service is running"
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Expense service is healthy", "Service is running normally"));
    }
}