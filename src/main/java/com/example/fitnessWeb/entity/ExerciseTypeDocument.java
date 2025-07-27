package com.example.fitnessWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
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

    private List<String> muscleGroups;
}
