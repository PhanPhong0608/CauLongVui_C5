package com.example.CauLongVui.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Nullable để hỗ trợ user đăng nhập bằng Google (không có password)
    @Column(nullable = true)
    private String password;

    @Column(length = 20)
    private String phone;

    // Provider: LOCAL (đăng ký thường) hoặc GOOGLE (đăng nhập Google)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    // ID của user trên provider (Google sub ID)
    @Column(length = 255)
    private String providerId;

    // Avatar URL từ Google
    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, STAFF, CUSTOMER
    }

    public enum Provider {
        LOCAL, GOOGLE
    }
}
