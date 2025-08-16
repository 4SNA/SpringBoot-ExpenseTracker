package com.Project.ExpenseTracker.services;

import com.Project.ExpenseTracker.dto.ExpenseDTO;
import com.Project.ExpenseTracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {

    // New authenticated methods
    Expense createExpense(ExpenseDTO expenseDTO, String username);
    Page<Expense> getAllExpenses(String username, Pageable pageable);
    Expense getExpenseById(Long id, String username);
    Expense updateExpense(Long id, ExpenseDTO expenseDTO, String username);
    void deleteExpense(Long id, String username);

    Page<Expense> getExpensesByCategory(String category, String username, Pageable pageable);
    Double getTotalAmountByCategory(String category, String username);
    List<Expense> getCurrentMonthExpenses(String username);
    Double getCurrentMonthTotal(String username);

    // Advanced features
    Page<Expense> searchExpenses(String category, LocalDate startDate, LocalDate endDate,
                                 Double minAmount, Double maxAmount, String searchTerm,
                                 String username, Pageable pageable);
    List<Expense> createBulkExpenses(List<ExpenseDTO> expenseDTOs, String username);
    Map<String, Object> getExpenseAnalytics(String username);
    Map<String, Object> getExpenseSummary(LocalDate startDate, LocalDate endDate, String username);

    // Legacy methods (deprecated - for backward compatibility)
    @Deprecated
    Expense postExpense(ExpenseDTO expenseDTO);
    @Deprecated
    List<Expense> getAllExpenses();
    @Deprecated
    Expense getExpenseById(Long id);
    @Deprecated
    Expense updateExpense(Long id, ExpenseDTO expenseDTO);
    @Deprecated
    void deleteExpense(Long id);
    @Deprecated
    List<Expense> getExpensesByCategory(String category);
    @Deprecated
    Integer getTotalAmountByCategory(String category);
    @Deprecated
    List<Expense> getCurrentMonthExpenses();
    @Deprecated
    Double getCurrentMonthTotal();
}
