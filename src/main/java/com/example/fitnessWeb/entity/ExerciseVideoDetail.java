package com.example.fitnessWeb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseVideoDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String muscleGroup;     // 운동 부위 (예: 하체, 등)
    private String exerciseName;    // 운동 종목 (예: 데드리프트)
    private String athleteName;     // 선수명 (예: 김영희)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private ExerciseVideo video;
}
