package com.example.fitnessWeb.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", length = 50, nullable = false, unique = true)
    private String userId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private String userName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    private LocalDateTime createdAt;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Column(name = "xp", nullable = false)
    private int xp = 0;

    public void addXp(int amount) {
        this.xp += amount;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}