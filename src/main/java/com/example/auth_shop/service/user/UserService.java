package com.example.auth_shop.service.user;

import com.example.auth_shop.dto.UserDto;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.Role;
import com.example.auth_shop.model.User;
import com.example.auth_shop.repository.RoleRepository;
import com.example.auth_shop.repository.UserRepository;
import com.example.auth_shop.request.CreatedUserRequest;
import com.example.auth_shop.request.RegisterRequest;
import com.example.auth_shop.request.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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
    
    /**
     * Register new user (public registration)
     * 
     * Auto assigns ROLE_USER to new users
     * 
     * @param req RegisterRequest với user information
     * @return User entity sau khi đã được save
     * @throws AlreadyExistsException nếu email đã tồn tại
     */
    @Transactional
    public User register(RegisterRequest req) {
        // Check if email already exists
        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("Registration attempt with existing email: {}", req.getEmail());
            throw new AlreadyExistsException("Email already exists: " + req.getEmail());
        }
        
        // Find ROLE_USER
        List<Role> userRoles = roleRepository.findByName("ROLE_USER");
        if (userRoles.isEmpty()) {
            log.error("ROLE_USER not found. Cannot register user.");
            throw new ResourceNotFoundException("System error: ROLE_USER not found. Please contact administrator.");
        }
        
        Role userRole = userRoles.get(0);
        
        // Create new user
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setAccountNonLocked(true);  // New users are not locked
        user.setEnabled(true);  // New users are enabled
        user.setRoles(Set.of(userRole));  // Assign ROLE_USER
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", req.getEmail());
        
        return savedUser;
    }
}