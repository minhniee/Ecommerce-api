package com.example.auth_shop.service.user;

import com.example.auth_shop.dto.UserDto;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.User;
import com.example.auth_shop.repository.UserRepository;
import com.example.auth_shop.request.CreatedUserRequest;
import com.example.auth_shop.request.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id).orElseThrow( () ->  new ResourceNotFoundException("User not found"));
    }

    @Override
    public User createUser(CreatedUserRequest req) {
        return Optional.of(req).filter(user -> !userRepository.existsByEmail(user.getEmail()))
                .map(data -> {
                    User user = new User();
                    user.setEmail(req.getEmail());
                    user.setPassword(passwordEncoder.encode(req.getPassword()));
                    user.setFirstName(req.getFirstName());
                    user.setLastName(req.getLastName());
                    return userRepository.save(user);
                }).orElseThrow(() -> new ResourceNotFoundException( "Holy shiet"+req.getEmail() + " already exists!"));
    }

    @Override
    public User updateUser(UpdateUserRequest req, Long userId) {
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(req.getFirstName());
            existingUser.setLastName(req.getLastName());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new AlreadyExistsException("User not Exists!"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(userRepository::delete, () -> new ResourceNotFoundException("User not Exists!"));
    }

    @Override
    public UserDto convertToDTO(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}