package com.expensesplitter.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateExpenseRequest {

    @NotNull(message = "paidByUserId must not be null")
    private Long paidByUserId;

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
    private BigDecimal amount;

    private String description;

    @NotEmpty(message = "shares must contain at least one entry")
    private List<@Valid ExpenseShareRequest> shares;

}
