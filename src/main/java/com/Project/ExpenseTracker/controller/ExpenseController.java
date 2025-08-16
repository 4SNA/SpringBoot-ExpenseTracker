package com.Project.ExpenseTracker.controller;

import com.Project.ExpenseTracker.dto.ExpenseDTO;
import com.Project.ExpenseTracker.entity.Expense;
import com.Project.ExpenseTracker.services.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ExpenseController {

    private final ExpenseService expenseService;

    // CRUD Operations (5 endpoints)
    @PostMapping
    public ResponseEntity<?> createExpense(@Valid @RequestBody ExpenseDTO dto, Authentication authentication) {
        try {
            Expense createdExpense = expenseService.createExpense(dto, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating expense", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllExpenses(Authentication authentication, Pageable pageable) {
        try {
            Page<Expense> expenses = expenseService.getAllExpenses(authentication.getName(), pageable);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching expenses", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id, Authentication authentication) {
        try {
            Expense expense = expenseService.getExpenseById(id, authentication.getName());
            return ResponseEntity.ok(expense);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Expense not found", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching expense", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDTO dto,
                                           Authentication authentication) {
        try {
            Expense updatedExpense = expenseService.updateExpense(id, dto, authentication.getName());
            return ResponseEntity.ok(updatedExpense);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Expense not found", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error updating expense", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Authentication authentication) {
        try {
            expenseService.deleteExpense(id, authentication.getName());
            return ResponseEntity.ok(new SuccessResponse("Expense deleted successfully"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Expense not found", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error deleting expense", e.getMessage()));
        }
    }

    // Category Operations (2 endpoints)
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getExpensesByCategory(@PathVariable String category,
                                                   Authentication authentication,
                                                   Pageable pageable) {
        try {
            Page<Expense> expenses = expenseService.getExpensesByCategory(category, authentication.getName(), pageable);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching expenses by category", e.getMessage()));
        }
    }

    @GetMapping("/category/{category}/total")
    public ResponseEntity<?> getTotalAmountByCategory(@PathVariable String category,
                                                      Authentication authentication) {
        try {
            Double total = expenseService.getTotalAmountByCategory(category, authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "category", category,
                    "total", total != null ? total : 0.0,
                    "currency", "USD"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error calculating total", e.getMessage()));
        }
    }

    // Time-based Operations (2 endpoints)
    @GetMapping("/current-month")
    public ResponseEntity<?> getCurrentMonthExpenses(Authentication authentication) {
        try {
            List<Expense> expenses = expenseService.getCurrentMonthExpenses(authentication.getName());
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching current month expenses", e.getMessage()));
        }
    }

    @GetMapping("/current-month/total")
    public ResponseEntity<?> getCurrentMonthTotal(Authentication authentication) {
        try {
            Double total = expenseService.getCurrentMonthTotal(authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "month", LocalDate.now().getMonth(),
                    "year", LocalDate.now().getYear(),
                    "total", total != null ? total : 0.0,
                    "currency", "USD"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error calculating monthly total", e.getMessage()));
        }
    }

    // Additional Advanced Operations (4+ more endpoints to reach 10+)
    @GetMapping("/search")
    public ResponseEntity<?> searchExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String searchTerm,
            Authentication authentication,
            Pageable pageable) {
        try {
            Page<Expense> expenses = expenseService.searchExpenses(
                    category, startDate, endDate, minAmount, maxAmount, searchTerm,
                    authentication.getName(), pageable);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error searching expenses", e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBulkExpenses(@Valid @RequestBody List<ExpenseDTO> expenses,
                                                Authentication authentication) {
        try {
            List<Expense> createdExpenses = expenseService.createBulkExpenses(expenses, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Bulk expenses created successfully",
                    "count", createdExpenses.size(),
                    "expenses", createdExpenses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating bulk expenses", e.getMessage()));
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getExpenseAnalytics(Authentication authentication) {
        try {
            Map<String, Object> analytics = expenseService.getExpenseAnalytics(authentication.getName());
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error generating analytics", e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getExpenseSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {
        try {
            Map<String, Object> summary = expenseService.getExpenseSummary(
                    startDate, endDate, authentication.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error generating summary", e.getMessage()));
        }
    }

    // Response classes
    public record ErrorResponse(String error, String message) {}
    public record SuccessResponse(String message) {}
}
