package com.example.fitnessWeb.level.service;

import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDto;
import com.example.fitnessWeb.level.policy.ExperiencePolicy;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelService {

    private final ExperiencePolicy experiencePolicy;
    private final UserRepository userRepository;

    @Value("${level.base-xp}")
    private int baseXp;

    @Value("${level.exponent:1.5}") // 지수 값, 기본 1.5
    private double exponent;


    @Transactional
    public void processLevel(User user, List<ExerciseSetDto> exercises) {
        int xpGained = experiencePolicy.calculateXp(exercises);
        user.addXp(xpGained);

        // 레벨업 처리
        while (user.getXp() >= getXpForNextLevel(user.getLevel())) {
            user.setLevel(user.getLevel() + 1);
        }

        userRepository.save(user);

        System.out.println("이번 세션 경험치: " + xpGained + ", 누적 XP: " + user.getXp());
    }

    /**
     * 지수 증가로 레벨별 필요한 XP 계산
     * XP_next = baseXp * level^exponent
     */
    public int getXpForNextLevel(int level) {
        return (int) (baseXp * Math.pow(level, exponent));
    }

    public int getRemainingXpForNextLevel(User user) {
        int nextLevelXp = getXpForNextLevel(user.getLevel());
        return Math.max(nextLevelXp - user.getXp(), 0);
    }

    public double getLevelProgress(User user) {
        int currentLevel = user.getLevel();
        int prevLevelXp = currentLevel == 1 ? 0 : getTotalXpUpToLevel(currentLevel - 1);
        int nextLevelXp = getXpForNextLevel(currentLevel);

        return ((double)(user.getXp() - prevLevelXp) / (nextLevelXp - prevLevelXp)) * 100;
    }

    private int getTotalXpUpToLevel(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) {
            total += getXpForNextLevel(i);
        }
        return total;
    }
}