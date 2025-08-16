package com.Project.ExpenseTracker.services;

import com.Project.ExpenseTracker.entity.User;
import com.Project.ExpenseTracker.repository.ExpenseRepository;
import com.Project.ExpenseTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Cacheable(value = "categoryChart", key = "#username")
    public Map<String, Object> getCategoryWiseExpenses(String username) {
        User user = getUserByUsername(username);

        List<Object[]> categoryTotals = expenseRepository.getCategoryTotalsByUser(user);
        List<Object[]> categoryCounts = expenseRepository.getCategoryCountsByUser(user);

        Map<String, Object> chartData = new HashMap<>();

        List<Map<String, Object>> categories = new ArrayList<>();
        Map<String, Long> countMap = categoryCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).longValue()
                ));

        for (Object[] row : categoryTotals) {
            Map<String, Object> categoryData = new HashMap<>();
            String category = (String) row[0];
            Double total = ((Number) row[1]).doubleValue();
            Long count = countMap.getOrDefault(category, 0L);

            categoryData.put("category", category);
            categoryData.put("total", total);
            categoryData.put("count", count);
            categoryData.put("average", count > 0 ? total / count : 0.0);

            categories.add(categoryData);
        }

        chartData.put("type", "pie");
        chartData.put("title", "Expenses by Category");
        chartData.put("data", categories);
        chartData.put("totalCategories", categories.size());

        return chartData;
    }

    @Cacheable(value = "monthlyTrends", key = "#username + '_' + #months")
    public Map<String, Object> getMonthlyTrends(String username, int months) {
        User user = getUserByUsername(username);

        List<Object[]> monthlyTotals = expenseRepository.getMonthlyTotalsByUser(user);

        Map<String, Object> chartData = new HashMap<>();

        List<Map<String, Object>> trends = monthlyTotals.stream()
                .limit(months)
                .map(arr -> {
                    Map<String, Object> monthData = new HashMap<>();
                    Integer year = ((Number) arr[0]).intValue();
                    Integer month = ((Number) arr[1]).intValue();
                    Double total = ((Number) arr[2]).doubleValue();

                    monthData.put("year", year);
                    monthData.put("month", month);
                    monthData.put("monthName", getMonthName(month));
                    monthData.put("total", total);
                    monthData.put("period", year + "-" + String.format("%02d", month));

                    return monthData;
                })
                .collect(Collectors.toList());

        chartData.put("type", "line");
        chartData.put("title", "Monthly Expense Trends");
        chartData.put("data", trends);
        chartData.put("months", months);

        return chartData;
    }

    public Map<String, Object> getDailyExpenses(String username, LocalDate startDate, LocalDate endDate) {
        User user = getUserByUsername(username);

        List<Object[]> dailyTotals = expenseRepository.getDailyTotalsByUser(user, startDate);

        Map<String, Object> chartData = new HashMap<>();

        List<Map<String, Object>> dailyData = dailyTotals.stream()
                .filter(arr -> {
                    LocalDate date = ((java.sql.Date) arr[0]).toLocalDate();
                    return !date.isBefore(startDate) && !date.isAfter(endDate);
                })
                .map(arr -> {
                    Map<String, Object> dayData = new HashMap<>();
                    LocalDate date = ((java.sql.Date) arr[0]).toLocalDate();
                    Double total = ((Number) arr[1]).doubleValue();

                    dayData.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    dayData.put("total", total);
                    dayData.put("dayOfWeek", date.getDayOfWeek().name());

                    return dayData;
                })
                .collect(Collectors.toList());

        chartData.put("type", "bar");
        chartData.put("title", "Daily Expenses");
        chartData.put("data", dailyData);
        chartData.put("startDate", startDate);
        chartData.put("endDate", endDate);

        return chartData;
    }

    @Cacheable(value = "expenseDistribution", key = "#username")
    public Map<String, Object> getExpenseDistribution(String username) {
        User user = getUserByUsername(username);

        Double maxExpense = expenseRepository.getMaxExpenseByUser(user);
        Double minExpense = expenseRepository.getMinExpenseByUser(user);

        if (maxExpense == null || minExpense == null) {
            return Map.of(
                    "type", "histogram",
                    "title", "Expense Distribution",
                    "data", Collections.emptyList(),
                    "message", "No expenses found"
            );
        }

        // Create distribution buckets
        double range = maxExpense - minExpense;
        int buckets = 10;
        double bucketSize = range / buckets;

        Map<String, Object> chartData = new HashMap<>();
        List<Map<String, Object>> distribution = new ArrayList<>();

        for (int i = 0; i < buckets; i++) {
            double rangeStart = minExpense + (i * bucketSize);
            double rangeEnd = minExpense + ((i + 1) * bucketSize);

            Map<String, Object> bucket = new HashMap<>();
            bucket.put("range", String.format("%.2f - %.2f", rangeStart, rangeEnd));
            bucket.put("rangeStart", rangeStart);
            bucket.put("rangeEnd", rangeEnd);
            bucket.put("count", 0); // You'd need to implement the actual counting logic

            distribution.add(bucket);
        }

        chartData.put("type", "histogram");
        chartData.put("title", "Expense Amount Distribution");
        chartData.put("data", distribution);
        chartData.put("minExpense", minExpense);
        chartData.put("maxExpense", maxExpense);

        return chartData;
    }

    @Cacheable(value = "dashboardData", key = "#username")
    public Map<String, Object> getDashboardData(String username) {
        User user = getUserByUsername(username);

        Map<String, Object> dashboard = new HashMap<>();

        // Key metrics
        Long totalExpenses = expenseRepository.countExpensesByUser(user);
        Double totalAmount = expenseRepository.getTotalAmountByUser(user);
        Double averageExpense = expenseRepository.getAverageExpenseByUser(user);

        // Current month data
        Double currentMonthTotal = getCurrentMonthTotal(user);

        // Top categories (top 5)
        List<Object[]> topCategories = expenseRepository.getCategoryTotalsByUser(user)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        dashboard.put("totalExpenses", totalExpenses != null ? totalExpenses : 0L);
        dashboard.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
        dashboard.put("averageExpense", averageExpense != null ? averageExpense : 0.0);
        dashboard.put("currentMonthTotal", currentMonthTotal);
        dashboard.put("topCategories", topCategories);
        dashboard.put("generatedAt", java.time.LocalDateTime.now());

        return dashboard;
    }

    private Double getCurrentMonthTotal(User user) {
        return expenseRepository.findCurrentMonthExpensesByUser(user).stream()
                .mapToDouble(expense -> expense.getAmount() != null ? expense.getAmount() : 0.0)
                .sum();
    }

    private String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return months[month - 1];
    }
}
