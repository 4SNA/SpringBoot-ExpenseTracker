package com.Project.ExpenseTracker.repository;

import com.Project.ExpenseTracker.entity.Expense;
import com.Project.ExpenseTracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // User-specific queries
    List<Expense> findByUserOrderByDateDesc(User user);

    Page<Expense> findByUserOrderByDateDesc(User user, Pageable pageable);

    Optional<Expense> findByIdAndUser(Long id, User user);

    List<Expense> findByUserAndCategory(User user, String category);

    List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Expense> findByUserAndAmountGreaterThan(User user, Double amount);

    List<Expense> findByUserAndTitleContainingIgnoreCase(User user, String title);

    // Aggregation queries for user
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user")
    Double getTotalAmountByUser(@Param("user") User user);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.category = :category")
    Double getTotalAmountByUserAndCategory(@Param("user") User user, @Param("category") String category);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate")
    Double getTotalAmountByUserAndDateRange(@Param("user") User user,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // Current month expenses for user
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND MONTH(e.date) = MONTH(CURRENT_DATE) AND YEAR(e.date) = YEAR(CURRENT_DATE) ORDER BY e.date DESC")
    List<Expense> findCurrentMonthExpensesByUser(@Param("user") User user);

    // Category statistics for user
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryTotalsByUser(@Param("user") User user);

    @Query("SELECT e.category, COUNT(e) FROM Expense e WHERE e.user = :user GROUP BY e.category ORDER BY COUNT(e) DESC")
    List<Object[]> getCategoryCountsByUser(@Param("user") User user);

    // Monthly statistics for user
    @Query("SELECT YEAR(e.date), MONTH(e.date), SUM(e.amount) FROM Expense e WHERE e.user = :user GROUP BY YEAR(e.date), MONTH(e.date) ORDER BY YEAR(e.date) DESC, MONTH(e.date) DESC")
    List<Object[]> getMonthlyTotalsByUser(@Param("user") User user);

    @Query("SELECT DATE(e.date), SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.date >= :startDate GROUP BY DATE(e.date) ORDER BY DATE(e.date)")
    List<Object[]> getDailyTotalsByUser(@Param("user") User user, @Param("startDate") LocalDate startDate);

    // Advanced search
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND " +
            "(:category IS NULL OR e.category = :category) AND " +
            "(:startDate IS NULL OR e.date >= :startDate) AND " +
            "(:endDate IS NULL OR e.date <= :endDate) AND " +
            "(:minAmount IS NULL OR e.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR e.amount <= :maxAmount) AND " +
            "(:searchTerm IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Expense> findExpensesWithFilters(@Param("user") User user,
                                          @Param("category") String category,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("minAmount") Double minAmount,
                                          @Param("maxAmount") Double maxAmount,
                                          @Param("searchTerm") String searchTerm,
                                          Pageable pageable);

    // Analytics queries
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user = :user")
    Long countExpensesByUser(@Param("user") User user);

    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.user = :user")
    Double getAverageExpenseByUser(@Param("user") User user);

    @Query("SELECT MAX(e.amount) FROM Expense e WHERE e.user = :user")
    Double getMaxExpenseByUser(@Param("user") User user);

    @Query("SELECT MIN(e.amount) FROM Expense e WHERE e.user = :user")
    Double getMinExpenseByUser(@Param("user") User user);

    // Legacy methods for backward compatibility
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
