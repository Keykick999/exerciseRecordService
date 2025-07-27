package com.example.fitnessWeb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exercise_type_id", "muscle_id"})
})
public class ExerciseMuscle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_type_id")
    private ExerciseType exerciseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muscle_id")
    private Muscle muscle;
}
