package com.example.auth_shop.security.user;

import com.example.auth_shop.model.User;
import com.example.auth_shop.service.AccountLockoutService;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ShopUserDetails implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private Collection<GrantedAuthority> authorities;
    
    // Account status fields
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;  // Combined: DB permanent lock OR Redis temporary lock
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    /**
     * Build UserDetails from User entity
     * 
     * Checks both:
     * 1. Permanent lock (database) - admin can lock/unlock
     * 2. Temporary lock (Redis) - auto lockout after failed attempts
     * 
     * Account is locked if EITHER permanent OR temporary lock is active
     */
    public static ShopUserDetails buildUserDetails(User user, AccountLockoutService accountLockoutService) {
        List<GrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Check permanent lock (database) - admin lock
        boolean permanentLock = !user.isAccountNonLocked();
        
        // Check temporary lock (Redis) - failed attempts lockout
        boolean temporaryLock = accountLockoutService.isAccountLockedInRedis(user.getEmail());
        
        // Account is locked if EITHER permanent OR temporary lock is active
        boolean accountNonLocked = !permanentLock && !temporaryLock;

        return new ShopUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                true,  // accountNonExpired
                accountNonLocked,  // Combined lock status
                true,  // credentialsNonExpired
                user.isEnabled()   // enabled from database
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


}
