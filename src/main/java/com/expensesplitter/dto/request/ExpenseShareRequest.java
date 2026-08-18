package com.expensesplitter.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExpenseShareRequest {

    @NotNull(message = "userId must not be null")
    private Long userId;

    @NotNull(message = "shareAmount must not be null")
    @DecimalMin(value = "0.01", message = "shareAmount must be at least 0.01")
    private BigDecimal shareAmount;

}
