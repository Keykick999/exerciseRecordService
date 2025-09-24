package com.example.fitnessWeb.exerciseVideo.domain;
import com.example.fitnessWeb.exerciseVideo.enums.ExerciseVideoType;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_exercise_video_type", columnList = "type")
        }
)
public class ExerciseVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ExerciseVideoType type;

    @Column(name = "iframe_src", nullable = false, unique = true)
    private String iframeSrc;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseVideoDetail> details;
}