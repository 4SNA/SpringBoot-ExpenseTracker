package com.Project.ExpenseTracker.services;

import com.Project.ExpenseTracker.dto.ExpenseDTO;
import com.Project.ExpenseTracker.entity.Expense;

import java.util.List;

public interface ExpenseService {

    Expense postExpense(ExpenseDTO expenseDTO);

    List<Expense> getAllExpenses();

    Expense getExpenseById(Long id);

    Expense updateExpense(Long id, ExpenseDTO expenseDTO);

    void deleteExpense(Long id);

    List<Expense> getExpensesByCategory(String category);

    Integer getTotalAmountByCategory(String category);

    List<Expense> getCurrentMonthExpenses();

    Double getCurrentMonthTotal();
}
