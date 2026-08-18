package com.expensesplitter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MemberBalanceResponse {

    private final Long userId;
    private final BigDecimal netBalance;

    public static MemberBalanceResponse from(Long userId, BigDecimal netBalance) {
        return new MemberBalanceResponse(userId, netBalance);
    }

}
