package com.example.auth_shop.data;

import com.example.auth_shop.model.Role;
import com.example.auth_shop.model.User;
import com.example.auth_shop.repository.RoleRepository;
import com.example.auth_shop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * DataInitial - Khởi tạo dữ liệu mặc định khi application start
 * 
 * GIẢI THÍCH VỀ ApplicationListener<ApplicationReadyEvent>:
 * =========================================================
 * 
 * ApplicationReadyEvent được fire khi Spring Boot application đã sẵn sàng
 * và tất cả beans đã được khởi tạo. Đây là thời điểm tốt để:
 * - Khởi tạo dữ liệu mặc định
 * - Chạy migrations
 * - Setup initial data
 * 
 * THỨ TỰ QUAN TRỌNG:
 * ==================
 * 1. Tạo roles TRƯỚC
 * 2. Sau đó mới tạo users (vì users cần roles)
 * 
 * @Transactional:
 * ===============
 * Đảm bảo tất cả operations trong method được thực hiện trong một transaction
 * Nếu có lỗi → rollback tất cả
 */
@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
public class DataInitial implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.info("Initializing default data...");
        
        Set<String> defaultRoles = Set.of("ROLE_ADMIN", "ROLE_USER");
        
        // QUAN TRỌNG: Tạo roles TRƯỚC khi tạo users
        // Vì users cần reference đến roles
        createDefaultRoleIfNotExist(defaultRoles);
        
        // Sau đó mới tạo users
        createDefaultUserIfNotExist();
        createDefaultAdminIfNotExist();
        
        log.info("Default data initialization completed");
    }

    /**
     * Tạo default users với ROLE_USER
     * 
     * LƯU Ý: Phải được gọi SAU createDefaultRoleIfNotExist()
     */
    private void createDefaultUserIfNotExist() {
        // Tìm role ROLE_USER
        List<Role> userRoles = roleRepository.findByName("ROLE_USER");
        
        // Kiểm tra role có tồn tại không
        if (userRoles.isEmpty()) {
            log.warn("ROLE_USER not found. Skipping default user creation.");
            return;
        }
        
        Role userRole = userRoles.get(0);
        
        for (int i = 0; i < 5; i++) {
            String defaultUser = "email" + i + "@gmail.com";
            
            // Skip nếu user đã tồn tại
            if (userRepository.existsByEmail(defaultUser)) {
                continue;
            }
            
            User user = new User();
            user.setFirstName("fUser" + i);
            user.setLastName("lUser" + i);
            user.setEmail(defaultUser);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRoles(Set.of(userRole));
            
            userRepository.save(user);
            log.info("Created default user: {}", defaultUser);
        }
    }

    /**
     * Tạo default roles nếu chưa tồn tại
     * 
     * Được gọi ĐẦU TIÊN để đảm bảo roles tồn tại trước khi tạo users
     */
    private void createDefaultRoleIfNotExist(Set<String> roles) {
        roles.stream()
                .filter(role -> roleRepository.findByName(role).isEmpty())
                .forEach(roleName -> {
                    Role role = new Role(roleName);
                    roleRepository.save(role);
                    log.info("Created default role: {}", role.getName());
                });
    }

    /**
     * Tạo default admin users với ROLE_ADMIN
     * 
     * LƯU Ý: Phải được gọi SAU createDefaultRoleIfNotExist()
     */
    private void createDefaultAdminIfNotExist() {
        // Tìm role ROLE_ADMIN
        List<Role> adminRoles = roleRepository.findByName("ROLE_ADMIN");
        
        // Kiểm tra role có tồn tại không
        if (adminRoles.isEmpty()) {
            log.warn("ROLE_ADMIN not found. Skipping default admin creation.");
            return;
        }
        
        Role adminRole = adminRoles.get(0);
        
        for (int i = 0; i < 5; i++) {
            String defaultUser = "Admin" + i + "@gmail.com";
            
            // Skip nếu user đã tồn tại
            if (userRepository.existsByEmail(defaultUser)) {
                continue;
            }
            
            User user = new User();
            user.setFirstName("admin" + i);
            user.setLastName("admin" + i);
            user.setEmail(defaultUser);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRoles(Set.of(adminRole));
            
            userRepository.save(user);
            log.info("Created default admin: {}", defaultUser);
        }
    }
}
