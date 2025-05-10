package com.example.fitnessWeb.service;

import com.example.fitnessWeb.config.JwtUtil;
import com.example.fitnessWeb.dto.UserLoginRequest;
import com.example.fitnessWeb.dto.UserRegisterRequest;
import com.example.fitnessWeb.entity.User;
import com.example.fitnessWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(UserRegisterRequest dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자 이름입니다.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .build();
        userRepository.save(user);
    }

    public String login(UserLoginRequest dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return jwtUtil.generateToken(user.getUsername());
    }
}
