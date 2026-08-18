package com.expensesplitter.repository;

import com.expensesplitter.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    List<ExpenseShare> findByExpenseId(Long expenseId);

    List<ExpenseShare> findByExpenseIdIn(Collection<Long> expenseIds);

}
