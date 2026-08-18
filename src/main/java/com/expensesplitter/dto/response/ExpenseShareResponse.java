package com.expensesplitter.dto.response;

import com.expensesplitter.entity.ExpenseShare;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ExpenseShareResponse {

    private final Long userId;
    private final BigDecimal shareAmount;

    public static ExpenseShareResponse from(ExpenseShare expenseShare) {
        return new ExpenseShareResponse(expenseShare.getUserId(), expenseShare.getShareAmount());
    }

}
