package com.example.fitnessWeb.level.controller;

import com.example.fitnessWeb.level.dto.LevelStatusDto;
import com.example.fitnessWeb.level.service.LevelService;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/level")
@RequiredArgsConstructor
public class LevelController {

    private final UserRepository userRepository;
    private final LevelService levelService;

    @GetMapping("/status")
    public ResponseEntity<LevelStatusDto> getLevelStatus(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findById(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        int remainingXp = levelService.getRemainingXpForNextLevel(user);
        double progress = levelService.getLevelProgress(user);

        LevelStatusDto dto = LevelStatusDto.builder()
                .level(user.getLevel())
                .currentXp(user.getXp())
                .remainingXp(remainingXp)
                .progress(progress)
                .build();

        return ResponseEntity.ok(dto);
    }
}