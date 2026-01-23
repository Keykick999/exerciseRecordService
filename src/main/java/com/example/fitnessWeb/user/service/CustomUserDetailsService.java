package com.example.fitnessWeb.user.service;

import com.example.fitnessWeb.user.entity.CustomUserDetails;
import com.example.fitnessWeb.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username 파라미터 = 우리 시스템의 userId 로 사용
        com.example.fitnessWeb.user.entity.User user = users.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 권한 없이(빈 리스트) 반환. 비밀번호는 DB에 저장된 해시 그대로.
        return new CustomUserDetails(
            user.getUserName(),
            user.getPassword(),
            user.getEmail(),
            List.of()
        );
    }
}