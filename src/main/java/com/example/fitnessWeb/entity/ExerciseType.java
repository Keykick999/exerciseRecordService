package com.example.fitnessWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
                @Index(name = "idx_muscleGroup_name", columnList = "muscleGroup, name")
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExerciseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // 예: Bench Press

    @Column(nullable = false)
    private String muscleGroup; // 예: Chest, Back, Legs 등
}
