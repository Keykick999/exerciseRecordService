package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.RoutineDetailDto;
import com.example.fitnessWeb.dto.RoutineRequestDto;
import com.example.fitnessWeb.dto.RoutineSummaryDto;
import com.example.fitnessWeb.dto.RoutineUpdateRequestDto;
import com.example.fitnessWeb.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    @PostMapping
    public ResponseEntity<Void> createRoutine(@RequestBody RoutineRequestDto requestDto) {
        routineService.saveRoutine(requestDto);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<Page<RoutineSummaryDto>> getMyRoutines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RoutineSummaryDto> routines = routineService.getMyRoutines(page, size);
        return ResponseEntity.ok(routines);
    }

    @GetMapping("/{routineName}")
    public ResponseEntity<List<RoutineDetailDto>> getRoutineDetails(@PathVariable String routineName) {
        List<RoutineDetailDto> details = routineService.getRoutineDetails(routineName);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/{routineId}")
    public ResponseEntity<Void> updateRoutine(@PathVariable("routineId") Long routineId, @RequestBody RoutineUpdateRequestDto requestDto) {
        System.out.println("routineId = " + routineId);
        System.out.println("requestDto = " + requestDto);
        routineService.updateRoutine(routineId, requestDto);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> deleteRoutine(@PathVariable Long routineId) {
        routineService.deleteRoutine(routineId);
        return ResponseEntity.noContent().build();
    }
}
