package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.ExerciseSaveDto;
import com.example.fitnessWeb.dto.ExerciseSearchDto;
import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.service.ExerciseTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/api/exercise-types")
@RequiredArgsConstructor
public class ExerciseTypeController {

    private final ExerciseTypeService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid ExerciseSaveDto dto) {
        service.createExerciseType(dto);
        return ResponseEntity.ok("운동 종류 등록 완료");
    }


    // 운동 검색(부위명, 종목명)
    @GetMapping
    public ResponseEntity<Page<ExerciseType>> getAll(@RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "muscleGroup", required = false) HashSet<String> muscleGroups,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.searchExercise(name, muscleGroups, page, size));
    }
}








