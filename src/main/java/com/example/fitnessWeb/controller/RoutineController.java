package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.RoutineRequestDto;
import com.example.fitnessWeb.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    @PostMapping
    public ResponseEntity<Void> createRoutine(@RequestBody RoutineRequestDto requestDto) {
        System.out.println("requestDto = " + requestDto);
        System.out.println("requestDto.getRoutineSets() = " + requestDto.getRoutineSets());
        routineService.saveRoutine(requestDto);
        return ResponseEntity.ok().build();
    }
}
