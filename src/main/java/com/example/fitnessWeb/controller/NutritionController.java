package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.NutritionEntryCreateRequest;
import com.example.fitnessWeb.dto.NutritionEntryUpdateRequest;
import com.example.fitnessWeb.service.NutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    /** 엔트리 생성 (userId는 토큰에서 추출) */
    @PostMapping("/entries")
    public ResponseEntity<Long> create(@Valid @RequestBody NutritionEntryCreateRequest req) {
        Long id = nutritionService.createEntry(req);
        return ResponseEntity.ok(id);
    }

    /** 엔트리 수정 (userId는 토큰에서 추출) */
    @PutMapping("/entries/{entryId}")
    public ResponseEntity<Void> update(@PathVariable Long entryId,
                                       @Valid @RequestBody NutritionEntryUpdateRequest req) {
        nutritionService.updateEntry(entryId, req);
        return ResponseEntity.noContent().build();
    }

    /** 엔트리 소프트 삭제 (userId는 토큰에서 추출) */
    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> softDelete(@PathVariable Long entryId) {
        nutritionService.softDeleteEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}
