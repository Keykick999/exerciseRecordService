package com.example.fitnessWeb.exerciseVideo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_video_id", columnList = "video_id"),
                @Index(name = "idx_muscle_group", columnList = "muscleGroup"),
                @Index(name = "idx_exercise_name", columnList = "exerciseName"),
                @Index(name = "idx_athlete_name", columnList = "athleteName"),
        }
)
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
