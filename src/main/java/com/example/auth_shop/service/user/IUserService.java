package com.example.auth_shop.service.user;

import com.example.auth_shop.dto.UserDto;
import com.example.auth_shop.model.User;
import com.example.auth_shop.request.CreatedUserRequest;
import com.example.auth_shop.request.UpdateUserRequest;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    List<User> getUsers();
    Page<User> getUsers(Pageable pageable);
    User getUserById(Long id);
    User createUser(CreatedUserRequest req);
    User updateUser(UpdateUserRequest req, Long userId);
    void deleteUser(Long userId);


    UserDto convertToDTO(User user);

    User getAuthenticatedUser();
}
