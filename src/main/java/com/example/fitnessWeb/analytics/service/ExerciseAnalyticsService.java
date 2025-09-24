package com.example.fitnessWeb.analytics.service;

import com.example.fitnessWeb.analytics.dto.MuscleBalanceResponse;
import com.example.fitnessWeb.analytics.dto.WeeklyVolumeResponseDto;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.common.exception.SizeOutOfRangeException;
import com.example.fitnessWeb.common.exception.UserNotFoundException;
import com.example.fitnessWeb.analytics.projection.MaxRepsRow;
import com.example.fitnessWeb.analytics.projection.MaxVolumeSetRow;
import com.example.fitnessWeb.analytics.projection.MaxWeightRepsRow;
import com.example.fitnessWeb.analytics.projection.MuscleBalanceRow;
import com.example.fitnessWeb.analytics.repository.AnalyticsRepository;
import com.example.fitnessWeb.exerciseRecord.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExerciseAnalyticsService {

    private final ExerciseRecordRepository recordRepo;

    private final AnalyticsRepository analyticsRepository;

    private final UserRepository userRepository;

    public WeeklyVolumeResponseDto getWeeklyVolumeFor(String userId, LocalDate date) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        LocalDate target = (date != null) ? date : LocalDate.now();

        return recordRepo.findWeeklyVolumeForUserAndDate(userId, target)
                .map(p -> WeeklyVolumeResponseDto.builder()
                        .yearWeek(p.getYearWeek())
                        .weeklyVolume(p.getWeeklyVolume() == null ? 0.0 : p.getWeeklyVolume())
                        .build())
                .orElseGet(() -> WeeklyVolumeResponseDto.builder()
                        .yearWeek(yearWeekIso(target))
                        .weeklyVolume(0.0)
                        .build());
    }


    private int yearWeekIso(LocalDate date) {
        WeekFields wf = WeekFields.ISO; // 월요일 시작, ISO-8601
        int weekBasedYear = date.get(wf.weekBasedYear());
        int weekOfYear = date.get(wf.weekOfWeekBasedYear());
        return weekBasedYear * 100 + weekOfYear;
    }


    /**
     * 특정 회원(username), 특정 종목(exerciseTypeId), 특정 무게(weight)에 대하여,
     * 운동기록 단위로 최근 size회 동안의 "최대 reps"와 "운동 일시"를 반환
     */
    public List<MaxRepsRow> getRecentMaxRepsByWeight(
            String userId,
            Long exerciseTypeId,
            Integer weight,
            Integer size
    ) {
        // ---- 비즈니스 규칙 검증 ----
        if (size < 1 || size > 30) {
            throw new IllegalArgumentException("size는 1 이상 30 이하만 허용됩니다.");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight는 0 이상이어야 합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);


        return analyticsRepository.findRecentMaxRepsByWeight(
                user.getUserId(),
                exerciseTypeId,
                weight,
                PageRequest.of(0, size)
        );
    }


    public MuscleBalanceResponse getMuscleBalance(String userId,
                                                  LocalDate start,
                                                  LocalDate endExclusive) {
        LocalDate effectiveStart = (start != null) ? start : LocalDate.now().minusWeeks(4);
        LocalDate effectiveEndEx = (endExclusive != null) ? endExclusive : LocalDate.now().plusDays(1);

        LocalDateTime s = effectiveStart.atStartOfDay();
        LocalDateTime e = effectiveEndEx.atStartOfDay();

        List<MuscleBalanceRow> rows = analyticsRepository.findMuscleBalance(userId, s, e);

        long total = rows.stream()
                .map(r -> r.getTotalVolume() == null ? 0L : r.getTotalVolume())
                .findFirst().orElse(0L);

        List<MuscleBalanceResponse.Item> items = rows.stream()
                .map(r -> new MuscleBalanceResponse.Item(
                        r.getMuscle(),
                        r.getVolume() == null ? 0L : r.getVolume(),
                        total,
                        r.getPct()
                ))
                .toList();

        return new MuscleBalanceResponse(
                userId,
                effectiveStart.toString(),
                effectiveEndEx.toString(),
                total,
                items
        );
    }


    /**
     * 특정 회원(username), 특정 종목(exerciseTypeId)에 대하여
     * 운동기록별 "최대 무게 & 그 무게에서 최대 reps"를 최근 size회 조회
     */
    public List<MaxWeightRepsRow> getMaxWeightRepsHistory(
            String userId, Long exerciseTypeId, Integer size
    ) {
        if (size < 1 || size > 30) throw new SizeOutOfRangeException("size는 1이상 30이하만 허용됩니다.");

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return analyticsRepository.findMaxWeightRepsHistory(
                user.getUserId(),
                exerciseTypeId,
                PageRequest.of(0, size)
        );
    }


    /**
     * 운동기록별 최대 볼륨 세트를 최근 size회 조회 (옵션: 종목/기간 필터)
     */
    public List<MaxVolumeSetRow> getMaxVolumeSets(
            String userId,
            Long exerciseTypeId,         // nullable
            LocalDate start,             // nullable (일자 기준 inclusive)
            LocalDate endExclusive,      // nullable (일자 기준 exclusive)
            Integer size                 // 1~30
    ) {

        if (size < 1 || size > 30) throw new SizeOutOfRangeException("size는 1이상 30이하만 허용됩니다.");

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 날짜 경계 변환 (nullable 허용)
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = endExclusive != null ? endExclusive.atStartOfDay() : null;

        return analyticsRepository.findMaxVolumeSets(
                user.getUserId(),
                exerciseTypeId,
                startDateTime,
                endDateTime,
                PageRequest.of(0, size)
        );
    }
}