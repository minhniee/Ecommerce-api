package com.example.auth_shop.controller;


import com.example.auth_shop.dto.UserDto;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.User;
import com.example.auth_shop.request.CreatedUserRequest;
import com.example.auth_shop.request.UpdateUserRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/{userId}/user")
    public ResponseEntity<APIResponse> getUserById(@PathVariable Long userId) {
        try {

            User user = userService.getUserById(userId);
            UserDto userDto  = userService.convertToDTO(user);
            return ResponseEntity.ok(new APIResponse("Success", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity. status(HttpStatus.NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }
    @PostMapping("/user/create")
    public ResponseEntity<APIResponse> createUser(@Valid @RequestBody CreatedUserRequest req) {
        try {
            User user = userService.createUser(req);
            UserDto userDto  = userService.convertToDTO(user);

            return ResponseEntity.ok(new APIResponse("Create User Success", userDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity. status(HttpStatus.CONFLICT).body(new APIResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<APIResponse> updateUser(@Valid @RequestBody UpdateUserRequest req, @PathVariable Long userId) {
        try {
            User user = userService.updateUser(req,userId);
            UserDto userDto = userService.convertToDTO(user);
            return ResponseEntity.ok(new APIResponse("Update User Success", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity. status(HttpStatus.NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<APIResponse> deleteUser(@PathVariable Long userId) {
        try {
             userService.deleteUser(userId);
            return ResponseEntity.ok(new APIResponse("Update User Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity. status(HttpStatus.NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }
}
