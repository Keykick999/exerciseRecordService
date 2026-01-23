package com.example.fitnessWeb.exerciseRecord.controller;

import com.example.fitnessWeb.exerciseRecord.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDto;
import com.example.fitnessWeb.exerciseRecord.service.ExerciseRecordService;
import com.example.fitnessWeb.user.entity.CustomUserDetails;
import com.example.fitnessWeb.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/exercise-records")
@RequiredArgsConstructor
public class ExerciseRecordController {

    private final ExerciseRecordService service;


    // 운동 기록 저장
    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid List<ExerciseSetDto> dto,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        service.saveRecord(userDetails.getUsername(), dto);
        return ResponseEntity.ok("운동 기록 저장 완료");
    }


    // 운동 기록 조회
    @GetMapping
    public ResponseEntity<Slice<ExerciseRecordResponseDto>> getExerciseRecord(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String exerciseName,
            @RequestParam(required = false) List<String> muscleGroup,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable,
            WebRequest request) {
        // eTag 비교
        String username = userDetails.getUsername();
        String email = userDetails.getEmail();
        Optional<LocalDateTime> lastModified = service.getLastModified(username);
        Optional<String> eTagOptional = generateEtag(email, lastModified);

        // eTag 변경 x -> 304
        if (eTagOptional.isPresent()) {
            String etag = eTagOptional.get();
            if (request.checkNotModified(etag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
        }

        // eTag가 변경 o -> 운동 기록 목록 조회
        Slice<ExerciseRecordResponseDto> result = service.getRecords(
                username,
                startDate,
                endDate,
                exerciseName,
                muscleGroup,
                pageable
        );

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        responseBuilder.cacheControl(CacheControl.noCache().cachePrivate().mustRevalidate());
        eTagOptional.ifPresent(responseBuilder::eTag);  // Etag가 있을 때만 헤더 추가

        return responseBuilder.body(result);
    }


    @GetMapping("/{recordId}/details")
    public ResponseEntity<List<ExerciseSetDetailDto>> getExerciseSetDetails(
            @PathVariable Long recordId,
            Authentication auth) {
        List<ExerciseSetDetailDto> result = service.getExerciseSetDetails(recordId, auth.getName());
        return ResponseEntity.ok(result);
    }

    // Etag 생성
    public Optional<String> generateEtag(String userEmail, Optional<LocalDateTime> lastModified) {
        if (lastModified.isEmpty()) return Optional.empty();    // 운동 기록이 없으면 eTag 생성 x

        String rawString = userEmail + lastModified.toString();
        return Optional.of(DigestUtils.md5DigestAsHex(rawString.getBytes(StandardCharsets.UTF_8)));
    }
}


