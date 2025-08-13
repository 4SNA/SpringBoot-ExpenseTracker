package com.Project.ExpenseTracker.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private String title;
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
}
