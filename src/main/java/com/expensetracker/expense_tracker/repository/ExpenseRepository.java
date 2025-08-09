package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // ==== AUTOMATIC METHODS (provided by JpaRepository) ====
    // save(expense) - Create/Update
    // findAll() - Get all expenses
    // findById(id) - Find by ID
    // deleteById(id) - Delete by ID
    // count() - Count total expenses
    // existsById(id) - Check if exists
    
    // ==== CUSTOM QUERY METHODS ====
    // Spring automatically creates these based on method names!
    
    /**
     * Find all expenses in a specific category
     * SQL: SELECT * FROM expenses WHERE category = ?
     */
    List<Expense> findByCategory(String category);
    
    /**
     * Find expenses greater than a specific amount
     * SQL: SELECT * FROM expenses WHERE amount > ?
     */
    List<Expense> findByAmountGreaterThan(BigDecimal amount);
    
    /**
     * Find expenses between two dates
     * SQL: SELECT * FROM expenses WHERE date BETWEEN ? AND ?
     */
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find expenses by category and sort by date descending
     * SQL: SELECT * FROM expenses WHERE category = ? ORDER BY date DESC
     */
    List<Expense> findByCategoryOrderByDateDesc(String category);
    
    /**
     * Find expenses containing text in description (case-insensitive)
     * SQL: SELECT * FROM expenses WHERE LOWER(description) LIKE LOWER(?)
     */
    List<Expense> findByDescriptionContainingIgnoreCase(String description);
    
    // ==== CUSTOM SQL QUERIES ====
    // For more complex queries, we can write custom SQL
    
    /**
     * Calculate total amount spent in a specific category
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category")
    BigDecimal calculateTotalByCategory(@Param("category") String category);
    
    /**
     * Get expenses for current month
     */
    @Query("SELECT e FROM Expense e WHERE YEAR(e.date) = YEAR(CURRENT_DATE) AND MONTH(e.date) = MONTH(CURRENT_DATE)")
    List<Expense> findExpensesForCurrentMonth();
    
    /**
     * Find top expensive transactions
     */
    @Query("SELECT e FROM Expense e ORDER BY e.amount DESC")
    List<Expense> findTopExpensiveTransactions();
}