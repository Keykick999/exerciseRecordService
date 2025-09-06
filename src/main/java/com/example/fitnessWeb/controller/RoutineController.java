package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.RoutineDetailDto;
import com.example.fitnessWeb.dto.RoutineRequestDto;
import com.example.fitnessWeb.dto.RoutineSummaryDto;
import com.example.fitnessWeb.dto.RoutineUpdateRequestDto;
import com.example.fitnessWeb.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    @PostMapping
    public ResponseEntity<Void> createRoutine(Authentication auth, @RequestBody RoutineRequestDto requestDto) {
        routineService.saveRoutine(auth.getName(), requestDto);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<Page<RoutineSummaryDto>> getMyRoutines(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RoutineSummaryDto> routines = routineService.getMyRoutines(auth.getName(), page, size);
        return ResponseEntity.ok(routines);
    }

    @GetMapping("/{routineName}")
    public ResponseEntity<List<RoutineDetailDto>> getRoutineDetails(Authentication auth, @PathVariable String routineName) {
        List<RoutineDetailDto> details = routineService.getRoutineDetails(auth.getName(), routineName);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/{routineId}")
    public ResponseEntity<Void> updateRoutine(Authentication auth, @PathVariable("routineId") Long routineId, @RequestBody RoutineUpdateRequestDto requestDto) {
        routineService.updateRoutine(auth.getName(), routineId, requestDto);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> deleteRoutine(Authentication auth, @PathVariable Long routineId) {
        routineService.deleteRoutine(auth.getName(), routineId);
        return ResponseEntity.noContent().build();
    }
}
