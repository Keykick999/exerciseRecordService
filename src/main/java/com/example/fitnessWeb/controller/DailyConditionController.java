package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.DailyConditionDto;
import com.example.fitnessWeb.service.DailyConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-conditions")
@RequiredArgsConstructor
public class DailyConditionController {

    private final DailyConditionService service;

    @PostMapping
    public ResponseEntity<String> save(
            @RequestBody @Valid DailyConditionDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        service.saveOrUpdate(userDetails.getUsername(), dto);
        return ResponseEntity.ok("컨디션 기록 저장 완료");
    }

    @GetMapping
    public ResponseEntity<List<DailyConditionDto>> getHistory(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.getConditionHistory(userDetails.getUsername(), start, end));
    }
}
