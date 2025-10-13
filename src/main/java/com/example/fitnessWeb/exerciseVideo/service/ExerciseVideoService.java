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
        ExerciseVideo video = createVideo(request);
        video.setDetails(createVideoDetails(video, request));

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

    /* ===================== Helper Methods ===================== */

    private ExerciseVideo createVideo(ExerciseVideoSaveRequest request) {
        return ExerciseVideo.builder()
                .iframeSrc(request.getIframeSrc())
                .type(request.getType())
                .build();
    }

    private List<ExerciseVideoDetail> createVideoDetails(ExerciseVideo video, ExerciseVideoSaveRequest request) {
        List<ExerciseVideoDetail> details = new ArrayList<>();
        for (String group : request.getMuscleGroups()) {
            for (String name : request.getExerciseNames()) {
                for (String athlete : request.getAthleteNames()) {
                    details.add(buildDetail(video, group, name, athlete));
                }
            }
        }
        return details;
    }

    private ExerciseVideoDetail buildDetail(ExerciseVideo video, String muscleGroup, String exerciseName, String athleteName) {
        return ExerciseVideoDetail.builder()
                .video(video)
                .muscleGroup(muscleGroup)
                .exerciseName(exerciseName)
                .athleteName(athleteName)
                .build();
    }

    public ExerciseVideoResponse toDto(ExerciseVideo video) {
        return ExerciseVideoResponse.builder()
                .id(video.getId())
                .type(video.getType())
                .iframeSrc(video.getIframeSrc())
                .details(
                        video.getDetails().stream()
                                .map(this::toDetailDto)
                                .toList()
                )
                .build();
    }

    private ExerciseVideoDetailResponse toDetailDto(ExerciseVideoDetail detail) {
        return ExerciseVideoDetailResponse.builder()
                .muscleGroup(detail.getMuscleGroup())
                .exerciseName(detail.getExerciseName())
                .athleteName(detail.getAthleteName())
                .build();
    }
}
