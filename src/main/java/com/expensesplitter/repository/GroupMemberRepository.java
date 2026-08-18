package com.expensesplitter.repository;

import com.expensesplitter.entity.GroupMember;
import com.expensesplitter.entity.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    List<GroupMember> findByIdUserId(Long userId);

    List<GroupMember> findByIdGroupId(Long groupId);

    List<GroupMember> findByIdGroupIdIn(Collection<Long> groupIds);

}
