package com.Project.ExpenseTracker.repository;

import com.Project.ExpenseTracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategory(String category);

    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Expense> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category")
    Integer getTotalAmountByCategory(@Param("category") String category);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate")
    Integer getTotalAmountByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    List<Expense> findByAmountGreaterThan(Integer amount);

    @Query("SELECT e FROM Expense e WHERE MONTH(e.date) = MONTH(CURRENT_DATE) AND YEAR(e.date) = YEAR(CURRENT_DATE)")
    List<Expense> findExpensesForCurrentMonth();
}
