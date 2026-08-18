package com.expensesplitter.service;

import com.expensesplitter.dto.request.CreateGroupRequest;
import com.expensesplitter.dto.response.GroupResponse;
import com.expensesplitter.entity.Group;
import com.expensesplitter.entity.GroupMember;
import com.expensesplitter.entity.GroupMemberId;
import com.expensesplitter.entity.User;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        // The creator is always a member, even if the client didn't list their id explicitly.
        // A LinkedHashSet both preserves the client's ordering and absorbs the creator id as a
        // harmless duplicate if it was already listed.
        Set<Long> memberUserIds = new LinkedHashSet<>(request.getMemberUserIds());
        memberUserIds.add(request.getCreatedByUserId());

        Set<Long> existingUserIds = userRepository.findAllById(memberUserIds).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        List<Long> missingUserIds = memberUserIds.stream()
                .filter(id -> !existingUserIds.contains(id))
                .toList();

        if (!missingUserIds.isEmpty()) {
            throw new IllegalArgumentException("No such user id(s): " + missingUserIds);
        }

        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedByUserId(request.getCreatedByUserId());
        Group savedGroup = groupRepository.save(group);

        List<GroupMember> members = memberUserIds.stream()
                .map(userId -> new GroupMember(new GroupMemberId(savedGroup.getId(), userId)))
                .toList();
        groupMemberRepository.saveAll(members);

        return GroupResponse.from(savedGroup, List.copyOf(memberUserIds));
    }

    public GroupResponse getGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group " + id + " not found"));

        List<Long> memberUserIds = groupMemberRepository.findByIdGroupId(id).stream()
                .map(member -> member.getId().getUserId())
                .toList();

        return GroupResponse.from(group, memberUserIds);
    }

    public List<GroupResponse> getGroupsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }

        List<Long> groupIds = groupMemberRepository.findByIdUserId(userId).stream()
                .map(member -> member.getId().getGroupId())
                .toList();

        List<Group> groups = groupRepository.findAllById(groupIds);

        Map<Long, List<Long>> memberUserIdsByGroupId = groupMemberRepository.findByIdGroupIdIn(groupIds).stream()
                .collect(Collectors.groupingBy(
                        member -> member.getId().getGroupId(),
                        Collectors.mapping(member -> member.getId().getUserId(), Collectors.toList())
                ));

        return groups.stream()
                .map(group -> GroupResponse.from(group, memberUserIdsByGroupId.getOrDefault(group.getId(), List.of())))
                .toList();
    }

}
