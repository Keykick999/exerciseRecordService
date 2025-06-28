package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.entity.ExerciseVideo;
import com.example.fitnessWeb.service.ExerciseVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class ExerciseVideoController {

    private final ExerciseVideoService exerciseVideoService;

    @GetMapping
    public Page<ExerciseVideo> searchVideos(
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String athleteName,
            @RequestParam(required = false) String category,
            Pageable pageable
    ) {
        return exerciseVideoService.searchVideos(muscleGroup, athleteName, category, pageable);
    }
}
