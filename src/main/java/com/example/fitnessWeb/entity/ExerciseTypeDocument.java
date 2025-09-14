package com.example.fitnessWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import java.util.List;
import org.springframework.data.elasticsearch.annotations.Document;


@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "exercise_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTypeDocument {

    @Id
    private String id;

    private String name;

    private List<String> muscleGroups;
}
