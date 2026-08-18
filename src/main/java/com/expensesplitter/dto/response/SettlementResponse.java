package com.expensesplitter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SettlementResponse {

    private final Long fromUserId;
    private final Long toUserId;
    private final BigDecimal amount;

    public static SettlementResponse from(Long fromUserId, Long toUserId, BigDecimal amount) {
        return new SettlementResponse(fromUserId, toUserId, amount);
    }

}
