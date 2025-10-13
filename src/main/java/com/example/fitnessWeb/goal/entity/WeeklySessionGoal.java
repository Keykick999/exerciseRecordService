package com.example.fitnessWeb.goal.entity;

import com.example.fitnessWeb.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "weekly_session_goal")
public class WeeklySessionGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int targetSessions; // 주간 목표 횟수
    private int achievedSessions; // 달성한 횟수
    private boolean achieved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
