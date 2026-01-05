package com.example.auth_shop.security.user;

import com.example.auth_shop.model.User;
import com.example.auth_shop.repository.UserRepository;
import com.example.auth_shop.service.AccountLockoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AccountLockoutService accountLockoutService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {  // get user
        User user = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Build UserDetails với cả permanent lock (DB) và temporary lock (Redis)
        return ShopUserDetails.buildUserDetails(user, accountLockoutService);
    }
}
