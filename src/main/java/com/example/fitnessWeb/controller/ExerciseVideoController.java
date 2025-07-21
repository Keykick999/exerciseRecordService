package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.ExerciseVideoResponse;
import com.example.fitnessWeb.dto.ExerciseVideoSaveRequest;
import com.example.fitnessWeb.dto.ExerciseVideoSearchRequest;
import com.example.fitnessWeb.service.ExerciseVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class ExerciseVideoController {

    private final ExerciseVideoService videoService;

    @PostMapping
    public ExerciseVideoResponse save(@RequestBody ExerciseVideoSaveRequest request) {
        return videoService.save(request);
    }

    @PostMapping("/search")
    public Page<ExerciseVideoResponse> search(@RequestBody ExerciseVideoSearchRequest request, Pageable pageable) {
        System.out.println("영상 검색");
        return videoService.search(request, pageable);
    }
}