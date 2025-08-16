package com.Project.ExpenseTracker.services.expense;

import com.Project.ExpenseTracker.dto.ExpenseDTO;
import com.Project.ExpenseTracker.entity.Expense;
import com.Project.ExpenseTracker.entity.User;
import com.Project.ExpenseTracker.repository.ExpenseRepository;
import com.Project.ExpenseTracker.repository.UserRepository;
import com.Project.ExpenseTracker.services.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public Expense createExpense(ExpenseDTO expenseDTO, String username) {
        User user = getUserByUsername(username);
        return saveOrUpdateExpense(new Expense(), expenseDTO, user);
    }

    @Override
    @Cacheable(value = "userExpenses", key = "#username + '_' + #pageable.pageNumber")
    public Page<Expense> getAllExpenses(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        return expenseRepository.findByUserOrderByDateDesc(user, pageable);
    }

    @Override
    public Expense getExpenseById(Long id, String username) {
        User user = getUserByUsername(username);
        return expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id: " + id));
    }

    @Override
    public Expense updateExpense(Long id, ExpenseDTO expenseDTO, String username) {
        User user = getUserByUsername(username);
        Expense existingExpense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id: " + id));
        return saveOrUpdateExpense(existingExpense, expenseDTO, user);
    }

    @Override
    public void deleteExpense(Long id, String username) {
        User user = getUserByUsername(username);
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id: " + id));
        expenseRepository.delete(expense);
    }

    @Override
    public Page<Expense> getExpensesByCategory(String category, String username, Pageable pageable) {
        User user = getUserByUsername(username);
        List<Expense> expenses = expenseRepository.findByUserAndCategory(user, category);
        return convertListToPage(expenses, pageable);
    }

    @Override
    public Double getTotalAmountByCategory(String category, String username) {
        User user = getUserByUsername(username);
        Double total = expenseRepository.getTotalAmountByUserAndCategory(user, category);
        return total != null ? total : 0.0;
    }

    @Override
    public List<Expense> getCurrentMonthExpenses(String username) {
        User user = getUserByUsername(username);
        return expenseRepository.findCurrentMonthExpensesByUser(user);
    }

    @Override
    public Double getCurrentMonthTotal(String username) {
        return getCurrentMonthExpenses(username).stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();
    }

    @Override
    public Page<Expense> searchExpenses(String category, LocalDate startDate, LocalDate endDate,
                                        Double minAmount, Double maxAmount, String searchTerm,
                                        String username, Pageable pageable) {
        User user = getUserByUsername(username);
        return expenseRepository.findExpensesWithFilters(
                user, category, startDate, endDate, minAmount, maxAmount, searchTerm, pageable);
    }

    @Override
    public List<Expense> createBulkExpenses(List<ExpenseDTO> expenseDTOs, String username) {
        User user = getUserByUsername(username);
        List<Expense> expenses = expenseDTOs.stream()
                .map(dto -> {
                    Expense expense = new Expense();
                    return saveOrUpdateExpense(expense, dto, user);
                })
                .collect(Collectors.toList());
        return expenseRepository.saveAll(expenses);
    }

    @Override
    @Cacheable(value = "expenseAnalytics", key = "#username")
    public Map<String, Object> getExpenseAnalytics(String username) {
        User user = getUserByUsername(username);

        Map<String, Object> analytics = new HashMap<>();

        // Basic statistics
        Long totalCount = expenseRepository.countExpensesByUser(user);
        Double totalAmount = expenseRepository.getTotalAmountByUser(user);
        Double averageAmount = expenseRepository.getAverageExpenseByUser(user);
        Double maxAmount = expenseRepository.getMaxExpenseByUser(user);
        Double minAmount = expenseRepository.getMinExpenseByUser(user);

        analytics.put("totalExpenses", totalCount != null ? totalCount : 0L);
        analytics.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
        analytics.put("averageAmount", averageAmount != null ? averageAmount : 0.0);
        analytics.put("maxAmount", maxAmount != null ? maxAmount : 0.0);
        analytics.put("minAmount", minAmount != null ? minAmount : 0.0);

        // Category breakdown
        List<Object[]> categoryTotals = expenseRepository.getCategoryTotalsByUser(user);
        Map<String, Double> categoryBreakdown = categoryTotals.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).doubleValue()
                ));
        analytics.put("categoryBreakdown", categoryBreakdown);

        // Monthly trends
        List<Object[]> monthlyTotals = expenseRepository.getMonthlyTotalsByUser(user);
        List<Map<String, Object>> monthlyData = monthlyTotals.stream()
                .limit(12) // Last 12 months
                .map(arr -> {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("year", arr[0]);
                    monthData.put("month", arr[1]);
                    monthData.put("total", ((Number) arr[2]).doubleValue());
                    return monthData;
                })
                .collect(Collectors.toList());
        analytics.put("monthlyTrends", monthlyData);

        return analytics;
    }

    @Override
    public Map<String, Object> getExpenseSummary(LocalDate startDate, LocalDate endDate, String username) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        User user = getUserByUsername(username);

        Map<String, Object> summary = new HashMap<>();

        Double totalAmount = expenseRepository.getTotalAmountByUserAndDateRange(user, startDate, endDate);
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
        summary.put("totalCount", expenses.size());
        summary.put("averageDaily", expenses.isEmpty() ? 0.0 :
                (totalAmount != null ? totalAmount : 0.0) / Math.max(1, startDate.until(endDate).getDays()));

        // Category summary for the period
        Map<String, Double> categorySummary = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                ));
        summary.put("categoryBreakdown", categorySummary);

        return summary;
    }

    private Expense saveOrUpdateExpense(Expense expense, ExpenseDTO expenseDTO, User user) {
        expense.setTitle(expenseDTO.getTitle());
        expense.setDescription(expenseDTO.getDescription());
        expense.setCategory(expenseDTO.getCategory());
        expense.setDate(expenseDTO.getDate());
        expense.setAmount(expenseDTO.getAmount());
        expense.setUser(user);

        if (expense.getId() == null) {
            expense.setCreatedAt(LocalDateTime.now());
        } else {
            expense.setUpdatedAt(LocalDateTime.now());
        }

        return expenseRepository.save(expense);
    }

    private Page<Expense> convertListToPage(List<Expense> expenses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), expenses.size());
        return new PageImpl<>(
                expenses.subList(start, end), pageable, expenses.size());
    }

    // Legacy methods for backward compatibility
    @Override
    public Expense postExpense(ExpenseDTO expenseDTO) {
        throw new UnsupportedOperationException("Use createExpense with username instead");
    }

    @Override
    public List<Expense> getAllExpenses() {
        throw new UnsupportedOperationException("Use getAllExpenses with username and pagination instead");
    }

    @Override
    public Expense getExpenseById(Long id) {
        throw new UnsupportedOperationException("Use getExpenseById with username instead");
    }

    @Override
    public Expense updateExpense(Long id, ExpenseDTO expenseDTO) {
        throw new UnsupportedOperationException("Use updateExpense with username instead");
    }

    @Override
    public void deleteExpense(Long id) {
        throw new UnsupportedOperationException("Use deleteExpense with username instead");
    }

    @Override
    public List<Expense> getExpensesByCategory(String category) {
        throw new UnsupportedOperationException("Use getExpensesByCategory with username and pagination instead");
    }

    @Override
    public Integer getTotalAmountByCategory(String category) {
        throw new UnsupportedOperationException("Use getTotalAmountByCategory with username instead");
    }

    @Override
    public List<Expense> getCurrentMonthExpenses() {
        throw new UnsupportedOperationException("Use getCurrentMonthExpenses with username instead");
    }

    @Override
    public Double getCurrentMonthTotal() {
        throw new UnsupportedOperationException("Use getCurrentMonthTotal with username instead");
    }
}
