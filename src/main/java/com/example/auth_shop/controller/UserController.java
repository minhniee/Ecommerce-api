package com.example.auth_shop.controller;

import com.example.auth_shop.dto.UserDto;
import com.example.auth_shop.model.User;
import com.example.auth_shop.request.CreatedUserRequest;
import com.example.auth_shop.request.UpdateUserRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<APIResponse> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        UserDto userDto = userService.convertToDTO(user);
        return ResponseEntity.ok(APIResponse.success("User retrieved successfully", userDto));
    }

    @GetMapping
    public ResponseEntity<APIResponse> getUsers() {
        List<User> users = userService.getUsers();
        List<UserDto> userDtos = users.stream()
                .map(userService::convertToDTO)
                .collect(Collectors.toList());
        // UserDto userDto = userService.convertToDTO(user);
        return ResponseEntity.ok(APIResponse.success("User retrieved successfully", userDtos));
    }


    @PostMapping
    public ResponseEntity<APIResponse> createUser(@Valid @RequestBody CreatedUserRequest req) {
        User user = userService.createUser(req);
        UserDto userDto = userService.convertToDTO(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.created("User created successfully", userDto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<APIResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest req, 
            @PathVariable Long userId) {
        User user = userService.updateUser(req, userId);
        UserDto userDto = userService.convertToDTO(user);
        return ResponseEntity.ok(APIResponse.success("User updated successfully", userDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<APIResponse> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(APIResponse.success("User deleted successfully"));
    }
}
