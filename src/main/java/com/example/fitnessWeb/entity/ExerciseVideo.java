package com.example.fitnessWeb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise_video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String iframeSrc;

    @Column(length = 100)
    private String muscleGroup;

    @Column(length = 100)
    private String athleteName;

    @Column(length = 50)
    private String category;  // 추가된 컬럼
}