package com.expensetracker.expense_tracker.dto.mapper;

import com.expensetracker.expense_tracker.dto.request.CreateExpenseRequest;
import com.expensetracker.expense_tracker.dto.request.UpdateExpenseRequest;
import com.expensetracker.expense_tracker.dto.response.ExpenseResponse;
import com.expensetracker.expense_tracker.dto.response.ExpenseListResponse;
import com.expensetracker.expense_tracker.entity.Expense;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpenseDTOMapper {

    // ==================== REQUEST DTO TO ENTITY ====================

    /**
     * Convert CreateExpenseRequest to Expense entity
     */
    public Expense toEntity(CreateExpenseRequest request) {
        if (request == null) {
            return null;
        }

        Expense expense = new Expense(
            request.getAmount(),
            request.getDescription(),
            request.getCategory(),
            request.getDate()
        );

        // Note: notes field will be added to entity later
        // expense.setNotes(request.getNotes());

        return expense;
    }

    /**
     * Update existing expense entity with UpdateExpenseRequest data
     */
    public void updateEntity(Expense expense, UpdateExpenseRequest request) {
        if (expense == null || request == null) {
            return;
        }

        // Only update fields that are provided (not null)
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }

        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }

        // Note: notes field will be added to entity later
        // if (request.getNotes() != null) {
        //     expense.setNotes(request.getNotes());
        // }
    }

    // ==================== ENTITY TO RESPONSE DTO ====================

    /**
     * Convert Expense entity to ExpenseResponse
     */
    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null) {
            return null;
        }

        return new ExpenseResponse(expense);
    }

    /**
     * Convert list of Expense entities to list of ExpenseResponse
     */
    public List<ExpenseResponse> toResponseList(List<Expense> expenses) {
        if (expenses == null) {
            return null;
        }

        return expenses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Expense entities to ExpenseListResponse (without pagination)
     */
    public ExpenseListResponse toListResponse(List<Expense> expenses) {
        if (expenses == null) {
            return ExpenseListResponse.of(List.of());
        }

        List<ExpenseResponse> responseList = toResponseList(expenses);
        return ExpenseListResponse.of(responseList);
    }

    /**
     * Convert list of Expense entities to ExpenseListResponse with pagination info
     */
    public ExpenseListResponse toListResponse(List<Expense> expenses, int page, int size, 
                                            long totalElements, int totalPages) {
        if (expenses == null) {
            return new ExpenseListResponse(List.of(), page, size, totalElements, totalPages);
        }

        List<ExpenseResponse> responseList = toResponseList(expenses);
        return new ExpenseListResponse(responseList, page, size, totalElements, totalPages);
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Create a new expense from request and convert to response
     */
    public ExpenseResponse createAndConvert(CreateExpenseRequest request) {
        Expense entity = toEntity(request);
        return toResponse(entity);
    }

    /**
     * Convert multiple entities to responses in a single call
     */
    public List<ExpenseResponse> convertAll(List<Expense> expenses) {
        return toResponseList(expenses);
    }

    // ==================== VALIDATION HELPERS ====================

    /**
     * Check if an UpdateExpenseRequest has any actual updates
     */
    public boolean hasUpdates(UpdateExpenseRequest request) {
        if (request == null) {
            return false;
        }

        return request.hasAnyUpdate();
    }

    /**
     * Create an expense entity with automatic field normalization
     * This is useful when we want to apply business rules during conversion
     */
    public Expense toEntityWithNormalization(CreateExpenseRequest request) {
        Expense expense = toEntity(request);
        
        if (expense != null) {
            // Apply any business rules during conversion
            // For example, normalize category names
            if (expense.getCategory() != null) {
                expense.setCategory(normalizeCategory(expense.getCategory()));
            }
            
            // Clean description
            if (expense.getDescription() != null) {
                expense.setDescription(cleanDescription(expense.getDescription()));
            }
        }
        
        return expense;
    }

    // ==================== PRIVATE UTILITY METHODS ====================

    /**
     * Normalize category names (same logic as in service)
     */
    private String normalizeCategory(String category) {
        if (category == null) return "Other";
        
        String normalized = category.trim();
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