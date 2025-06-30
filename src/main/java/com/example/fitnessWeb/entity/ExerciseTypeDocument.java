package com.example.fitnessWeb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "exercise-type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTypeDocument {

    @Id
    private String id; // Elasticsearch는 보통 String ID를 씁니다.

    private String name;
    private String muscleGroup;
}
