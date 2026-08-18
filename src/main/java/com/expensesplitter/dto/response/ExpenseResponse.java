package com.expensesplitter.dto.response;

import com.expensesplitter.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExpenseResponse {

    private final Long id;
    private final Long groupId;
    private final Long paidByUserId;
    private final BigDecimal amount;
    private final String description;
    private final Instant createdAt;
    private final List<ExpenseShareResponse> shares;

    public static ExpenseResponse from(Expense expense, List<ExpenseShareResponse> shares) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getGroupId(),
                expense.getPaidByUserId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getCreatedAt(),
                shares
        );
    }

}
