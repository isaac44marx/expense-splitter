package com.expensesplitter.service;

import com.expensesplitter.dto.request.CreateExpenseRequest;
import com.expensesplitter.dto.request.ExpenseShareRequest;
import com.expensesplitter.dto.response.ExpenseResponse;
import com.expensesplitter.dto.response.ExpenseShareResponse;
import com.expensesplitter.dto.response.GroupBalancesResponse;
import com.expensesplitter.dto.response.MemberBalanceResponse;
import com.expensesplitter.dto.response.SettlementResponse;
import com.expensesplitter.entity.Expense;
import com.expensesplitter.entity.ExpenseShare;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.ExpenseShareRepository;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;

    @Transactional
    public ExpenseResponse createExpense(Long groupId, CreateExpenseRequest request) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group " + groupId + " not found");
        }

        Set<Long> seenShareUserIds = new HashSet<>();
        Set<Long> duplicateShareUserIds = new LinkedHashSet<>();
        for (ExpenseShareRequest share : request.getShares()) {
            if (!seenShareUserIds.add(share.getUserId())) {
                duplicateShareUserIds.add(share.getUserId());
            }
        }
        if (!duplicateShareUserIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Duplicate userId(s) in shares: " + duplicateShareUserIds);
        }

        Set<Long> memberUserIds = groupMemberRepository.findByIdGroupId(groupId).stream()
                .map(member -> member.getId().getUserId())
                .collect(Collectors.toSet());

        Set<Long> nonMemberUserIds = new LinkedHashSet<>();
        if (!memberUserIds.contains(request.getPaidByUserId())) {
            nonMemberUserIds.add(request.getPaidByUserId());
        }
        for (ExpenseShareRequest share : request.getShares()) {
            if (!memberUserIds.contains(share.getUserId())) {
                nonMemberUserIds.add(share.getUserId());
            }
        }
        if (!nonMemberUserIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "User id(s) not a member of group " + groupId + ": " + nonMemberUserIds);
        }

        BigDecimal sharesTotal = request.getShares().stream()
                .map(ExpenseShareRequest::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sharesTotal.compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException(
                    "Shares must sum to the expense amount: expected " + request.getAmount()
                            + " but shares summed to " + sharesTotal);
        }

        Expense expense = new Expense();
        expense.setGroupId(groupId);
        expense.setPaidByUserId(request.getPaidByUserId());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseShare> shares = request.getShares().stream()
                .map(shareRequest -> {
                    ExpenseShare share = new ExpenseShare();
                    share.setExpenseId(savedExpense.getId());
                    share.setUserId(shareRequest.getUserId());
                    share.setShareAmount(shareRequest.getShareAmount());
                    return share;
                })
                .toList();
        List<ExpenseShare> savedShares = expenseShareRepository.saveAll(shares);

        List<ExpenseShareResponse> shareResponses = savedShares.stream()
                .map(ExpenseShareResponse::from)
                .toList();
        return ExpenseResponse.from(savedExpense, shareResponses);
    }

    public List<ExpenseResponse> listExpenses(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group " + groupId + " not found");
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<Long> expenseIds = expenses.stream().map(Expense::getId).toList();

        Map<Long, List<ExpenseShareResponse>> sharesByExpenseId = expenseShareRepository.findByExpenseIdIn(expenseIds).stream()
                .collect(Collectors.groupingBy(
                        ExpenseShare::getExpenseId,
                        Collectors.mapping(ExpenseShareResponse::from, Collectors.toList())
                ));

        return expenses.stream()
                .map(expense -> ExpenseResponse.from(expense, sharesByExpenseId.getOrDefault(expense.getId(), List.of())))
                .toList();
    }

    public GroupBalancesResponse getBalances(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group " + groupId + " not found");
        }

        List<Long> memberUserIds = groupMemberRepository.findByIdGroupId(groupId).stream()
                .map(member -> member.getId().getUserId())
                .toList();

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<Long> expenseIds = expenses.stream().map(Expense::getId).toList();
        List<ExpenseShare> shares = expenseShareRepository.findByExpenseIdIn(expenseIds);

        Map<Long, BigDecimal> netBalanceByUserId = new TreeMap<>();
        for (Long userId : memberUserIds) {
            netBalanceByUserId.put(userId, BigDecimal.ZERO.setScale(2));
        }
        for (Expense expense : expenses) {
            netBalanceByUserId.merge(expense.getPaidByUserId(), expense.getAmount(), BigDecimal::add);
        }
        for (ExpenseShare share : shares) {
            netBalanceByUserId.merge(share.getUserId(), share.getShareAmount().negate(), BigDecimal::add);
        }

        List<MemberBalanceResponse> balances = netBalanceByUserId.entrySet().stream()
                .map(entry -> MemberBalanceResponse.from(entry.getKey(), entry.getValue()))
                .toList();

        return GroupBalancesResponse.from(balances, settleUp(netBalanceByUserId));
    }

    private List<SettlementResponse> settleUp(Map<Long, BigDecimal> netBalanceByUserId) {
        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : netBalanceByUserId.entrySet()) {
            int comparison = entry.getValue().compareTo(BigDecimal.ZERO);
            if (comparison > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            } else if (comparison < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().negate()));
            }
        }
        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<SettlementResponse> settlements = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            Map.Entry<Long, BigDecimal> creditor = creditors.get(i);
            Map.Entry<Long, BigDecimal> debtor = debtors.get(j);
            BigDecimal amount = creditor.getValue().min(debtor.getValue());

            settlements.add(SettlementResponse.from(debtor.getKey(), creditor.getKey(), amount));

            creditor.setValue(creditor.getValue().subtract(amount));
            debtor.setValue(debtor.getValue().subtract(amount));

            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }
        return settlements;
    }

}
