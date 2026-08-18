package com.expensesplitter.service;

import com.expensesplitter.dto.request.CreateUserRequest;
import com.expensesplitter.dto.response.UserResponse;
import com.expensesplitter.entity.User;
import com.expensesplitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

}
