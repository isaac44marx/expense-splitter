package com.expensesplitter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateGroupRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "createdByUserId must not be null")
    private Long createdByUserId;

    @NotEmpty(message = "memberUserIds must contain at least one user id")
    private List<@NotNull(message = "memberUserIds must not contain null values") Long> memberUserIds;

}
