package com.expensesplitter.controller;

import com.expensesplitter.dto.request.CreateExpenseRequest;
import com.expensesplitter.dto.response.ExpenseResponse;
import com.expensesplitter.dto.response.GroupBalancesResponse;
import com.expensesplitter.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/groups/{groupId}/expenses")
    public List<ExpenseResponse> listExpenses(@PathVariable Long groupId) {
        return expenseService.listExpenses(groupId);
    }

    @GetMapping("/groups/{groupId}/balances")
    public GroupBalancesResponse getBalances(@PathVariable Long groupId) {
        return expenseService.getBalances(groupId);
    }

}
