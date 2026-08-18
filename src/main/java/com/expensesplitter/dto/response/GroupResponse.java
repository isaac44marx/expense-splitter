package com.expensesplitter.dto.response;

import com.expensesplitter.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupResponse {

    private final Long id;
    private final String name;
    private final Long createdByUserId;
    private final Instant createdAt;
    private final List<Long> memberUserIds;

    public static GroupResponse from(Group group, List<Long> memberUserIds) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCreatedByUserId(),
                group.getCreatedAt(),
                memberUserIds
        );
    }

}
