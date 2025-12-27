package com.example.auth_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * User Entity
 * 
 * LƯU Ý QUAN TRỌNG:
 * ================
 * PostgreSQL có từ khóa reserved là "user"
 * Nên phải đặt tên bảng là "users" (số nhiều) để tránh conflict
 * 
 * Các từ khóa reserved khác trong PostgreSQL:
 * - user, order, group, table, select, etc.
 * Nếu muốn dùng các từ này làm tên bảng, phải quote: "user"
 * Hoặc đổi tên: users, orders, groups, tables
 */
@Entity
@Table(name = "users")  // Đổi tên bảng từ "user" thành "users" để tránh conflict với PostgreSQL reserved keyword
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @NaturalId // ?
    private String email;
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // indicate that this entity does not own the relationship
    private List<Order> orders;

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles = new HashSet<>();
}
