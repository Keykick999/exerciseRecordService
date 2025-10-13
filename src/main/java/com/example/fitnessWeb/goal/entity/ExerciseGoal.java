package com.example.fitnessWeb.goal.entity;

import com.example.fitnessWeb.exerciseType.domain.ExerciseType;
import com.example.fitnessWeb.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exercise_goal")
public class ExerciseGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double weight; // 목표 중량
    private int reps;      // 목표 횟수
    private boolean achieved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_type_id")
    private ExerciseType exerciseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
