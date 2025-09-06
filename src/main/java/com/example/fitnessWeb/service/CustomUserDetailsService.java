//package com.example.fitnessWeb.service;
//
//import com.example.fitnessWeb.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.*;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//        return userRepository.findById(userId)
//                .map(user -> org.springframework.security.core.userdetails.User
//                        .withUsername(user.getUserId())
//                        .password(user.getPassword())
//                        .authorities("ROLE_USER")
//                        .build())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }
//}
