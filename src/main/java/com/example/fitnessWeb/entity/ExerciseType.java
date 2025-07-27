package com.example.fitnessWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_exercisetype_name", columnList = "name")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExerciseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "exerciseType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseMuscle> exerciseMuscles = new ArrayList<>();
}
