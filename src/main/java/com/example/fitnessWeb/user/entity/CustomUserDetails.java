package com.example.fitnessWeb.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final String email;

    public CustomUserDetails(String username, String password, String email,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities); // username은 식별자로 사용
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}