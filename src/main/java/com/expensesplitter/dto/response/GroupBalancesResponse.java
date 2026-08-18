package com.expensesplitter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupBalancesResponse {

    private final List<MemberBalanceResponse> balances;
    private final List<SettlementResponse> settlements;

    public static GroupBalancesResponse from(List<MemberBalanceResponse> balances, List<SettlementResponse> settlements) {
        return new GroupBalancesResponse(balances, settlements);
    }

}
