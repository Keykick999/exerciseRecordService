package com.example.fitnessWeb.exerciseVideo.service;

import com.example.fitnessWeb.exerciseVideo.dto.ExerciseVideoDetailResponse;
import com.example.fitnessWeb.exerciseVideo.dto.ExerciseVideoResponse;
import com.example.fitnessWeb.exerciseVideo.dto.ExerciseVideoSaveRequest;
import com.example.fitnessWeb.exerciseVideo.dto.ExerciseVideoSearchRequest;
import com.example.fitnessWeb.exerciseVideo.domain.ExerciseVideo;
import com.example.fitnessWeb.exerciseVideo.domain.ExerciseVideoDetail;
import com.example.fitnessWeb.exerciseVideo.repository.ExerciseVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseVideoService {

    private final ExerciseVideoRepository videoRepository;

    // 영상 저장
    public ExerciseVideoResponse save(ExerciseVideoSaveRequest request) {
        ExerciseVideo video = ExerciseVideo.builder()
                .iframeSrc(request.getIframeSrc())
                .type(request.getType())
                .build();

        List<ExerciseVideoDetail> details = new ArrayList<>();
        for (String group : request.getMuscleGroups()) {
            for (String name : request.getExerciseNames()) {
                for (String athlete : request.getAthleteNames()) {
                    details.add(ExerciseVideoDetail.builder()
                            .muscleGroup(group)
                            .exerciseName(name)
                            .athleteName(athlete)
                            .video(video)
                            .build());
                }
            }
        }

        video.setDetails(details);
        ExerciseVideo saved = videoRepository.save(video);
        return toDto(saved);
    }


    // 영상 검색
    public Page<ExerciseVideoResponse> search(ExerciseVideoSearchRequest request, Pageable pageable) {
        return videoRepository.searchVideos(
                request.getMuscleGroups(),
                request.getExerciseNames(),
                request.getAthleteNames(),
                request.getType(),
                pageable
        ).map(this::toDto);
    }


    public ExerciseVideoResponse toDto(ExerciseVideo video) {
        return ExerciseVideoResponse.builder()
                .id(video.getId())
                .type(video.getType())
                .iframeSrc(video.getIframeSrc())
                .details(
                        video.getDetails().stream()
                                .map(detail -> ExerciseVideoDetailResponse.builder()
                                        .muscleGroup(detail.getMuscleGroup())
                                        .exerciseName(detail.getExerciseName())
                                        .athleteName(detail.getAthleteName())
                                        .build())
                                .toList()
                )
                .build();
    }
}