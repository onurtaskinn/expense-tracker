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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/dto-test")
@Tag(name = "DTO Testing", description = "Testing endpoints using DTOs for clean API design")
public class DTOTestController {

    @Autowired
    private ExpenseService expenseService;

    // ==================== CREATE OPERATIONS ====================

    @PostMapping("/create-sample-data")
    @Operation(summary = "Create sample data using DTOs", description = "Creates sample expenses using DTO-based service methods")
    public ResponseEntity<ApiResponse<String>> createSampleData() {
        try {
            // Create sample expenses using DTOs
            CreateExpenseRequest req1 = new CreateExpenseRequest(
                new BigDecimal("25.50"), "Lunch at McDonald's", "food", LocalDate.now());
            expenseService.createExpense(req1);

            CreateExpenseRequest req2 = new CreateExpenseRequest(
                new BigDecimal("60.00"), "Weekly groceries", "Food", LocalDate.now());
            expenseService.createExpense(req2);

            CreateExpenseRequest req3 = new CreateExpenseRequest(
                new BigDecimal("30.00"), "Gas station fill-up", "transportation", LocalDate.now());
            expenseService.createExpense(req3);

            CreateExpenseRequest req4 = new CreateExpenseRequest(
                new BigDecimal("15.75"), "Coffee and pastry", "dining", LocalDate.now().minusDays(1));
            expenseService.createExpense(req4);

            CreateExpenseRequest req5 = new CreateExpenseRequest(
                new BigDecimal("120.00"), "Monthly Metro pass", "Transport", LocalDate.now().minusDays(2));
            expenseService.createExpense(req5);

            return ResponseEntity.ok(ApiResponse.<String>success(null, "Sample data created successfully using DTOs!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/expenses")
    @Operation(summary = "Create expense with DTO", description = "Create a new expense using request DTO with validation")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {
        try {
            ExpenseResponse response = expenseService.createExpense(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Expense created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== READ OPERATIONS ====================

    @GetMapping("/expenses")
    @Operation(summary = "Get all expenses with DTOs", description = "Retrieves all expenses using response DTOs")
    public ResponseEntity<ApiResponse<ExpenseListResponse>> getAllExpenses() {
        try {
            ExpenseListResponse response = expenseService.getAllExpenses(null);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/expenses/{id}")
    @Operation(summary = "Get expense by ID with DTO", description = "Retrieves single expense using response DTO")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @Parameter(description = "Expense ID", example = "1") @PathVariable Long id) {
        try {
            ExpenseResponse response = expenseService.getExpenseById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/expenses/category/{category}")
    @Operation(summary = "Get expenses by category with DTOs", description = "Uses category normalization via service layer")
    public ResponseEntity<ApiResponse<ExpenseListResponse>> getExpensesByCategory(
            @Parameter(description = "Category (will be normalized)", example = "food") 
            @PathVariable String category) {
        try {
            ExpenseListResponse response = expenseService.getExpensesByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/expenses/search")
    @Operation(summary = "Search expenses with filter DTO", description = "Advanced search using ExpenseSearchRequest DTO")
    public ResponseEntity<ApiResponse<ExpenseListResponse>> searchExpenses(
            @Valid @RequestBody ExpenseSearchRequest searchRequest) {
        try {
            ExpenseListResponse response = expenseService.getAllExpenses(searchRequest);
            return ResponseEntity.ok(ApiResponse.success(response, "Search completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/expenses/search-simple")
    @Operation(summary = "Simple search by term", description = "Search expenses by description term")
    public ResponseEntity<ApiResponse<ExpenseListResponse>> searchByTerm(
            @Parameter(description = "Search term", example = "coffee") 
            @RequestParam String term) {
        try {
            ExpenseListResponse response = expenseService.searchExpenses(term);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== BUSINESS ANALYTICS ====================

    @GetMapping("/analytics/categories")
    @Operation(summary = "Category spending summary", description = "Get spending summary by category using response DTO")
    public ResponseEntity<ApiResponse<CategorySummaryResponse>> getCategorySpendingSummary() {
        try {
            CategorySummaryResponse response = expenseService.getCategorySpendingSummary();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/analytics/monthly-total")
    @Operation(summary = "Current month total", description = "Get total spending for current month")
    public ResponseEntity<ApiResponse<BigDecimal>> getCurrentMonthTotal() {
        try {
            BigDecimal total = expenseService.getCurrentMonthTotal();
            return ResponseEntity.ok(ApiResponse.success(total, "Current month total calculated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/analytics/monthly-total/{year}/{month}")
    @Operation(summary = "Monthly total for specific period", description = "Calculate total for specific month and year")
    public ResponseEntity<ApiResponse<BigDecimal>> getMonthlyTotal(
            @Parameter(description = "Year", example = "2024") @PathVariable int year,
            @Parameter(description = "Month (1-12)", example = "12") @PathVariable int month) {
        try {
            BigDecimal total = expenseService.getMonthlyTotal(year, month);
            String message = String.format("Total for %d/%d calculated", month, year);
            return ResponseEntity.ok(ApiResponse.success(total, message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/analytics/top-expensive")
    @Operation(summary = "Top expensive transactions", description = "Get top N most expensive transactions")
    public ResponseEntity<ApiResponse<ExpenseListResponse>> getTopExpensiveTransactions(
            @Parameter(description = "Number of transactions to return", example = "5") 
            @RequestParam(defaultValue = "5") int limit) {
        try {
            ExpenseListResponse response = expenseService.getTopExpensiveTransactions(limit);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== UPDATE/DELETE OPERATIONS ====================

    @PutMapping("/expenses/{id}")
    @Operation(summary = "Update expense with DTO", description = "Update expense using UpdateExpenseRequest DTO")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @Parameter(description = "Expense ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request) {
        try {
            ExpenseResponse response = expenseService.updateExpense(id, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Expense updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/expenses/{id}")
    @Operation(summary = "Delete expense", description = "Delete expense with business rule validation")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @Parameter(description = "Expense ID", example = "1") @PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== DEMO: SHOW DTO ADVANTAGES ====================

    @PostMapping("/demo/validation-errors")
    @Operation(summary = "Demo: Validation errors", description = "Demonstrates automatic DTO validation")
    public ResponseEntity<ApiResponse<ExpenseResponse>> demoValidationErrors(
            @Valid @RequestBody CreateExpenseRequest request) {
        // This will automatically validate the request DTO
        // Try sending invalid data to see validation errors in action
        try {
            ExpenseResponse response = expenseService.createExpense(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Expense created (validation passed)"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/demo/advanced-search")
    @Operation(summary = "Demo: Advanced search capabilities", 
               description = "Demonstrates complex filtering using ExpenseSearchRequest")
    public ResponseEntity<ApiResponse<ExpenseListResponse>> demoAdvancedSearch() {
        try {
            // Create a complex search request
            ExpenseSearchRequest searchRequest = new ExpenseSearchRequest();
            searchRequest.setMinAmount(new BigDecimal("20.00"));
            searchRequest.setMaxAmount(new BigDecimal("100.00"));
            searchRequest.setStartDate(LocalDate.now().minusDays(30));
            searchRequest.setEndDate(LocalDate.now());
            searchRequest.setSortBy("amount");
            searchRequest.setSortDirection("desc");

            ExpenseListResponse response = expenseService.getAllExpenses(searchRequest);
            return ResponseEntity.ok(ApiResponse.success(response, 
                "Advanced search: expenses between $20-$100 in last 30 days, sorted by amount"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}