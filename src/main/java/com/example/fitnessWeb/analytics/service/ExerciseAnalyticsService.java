package com.example.fitnessWeb.analytics.service;

import com.example.fitnessWeb.analytics.dto.*;
import com.example.fitnessWeb.analytics.projection.MaxRepsRow;
import com.example.fitnessWeb.analytics.projection.MaxWeightRepsRow;
import com.example.fitnessWeb.analytics.repository.AnalyticsNativeRepository;
import com.example.fitnessWeb.analytics.repository.AnalyticsRepository;
import com.example.fitnessWeb.common.exception.SizeOutOfRangeException;
import com.example.fitnessWeb.exerciseRecord.projection.WeeklyVolumeDto;
import com.example.fitnessWeb.exerciseRecord.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseAnalyticsService {

    private final ExerciseRecordRepository recordRepo;
    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsNativeRepository analyticsNativeRepository;
    private final AuthService userService;

    // ==========================
    // 높은 추상화: 외부에서 호출되는 서비스 메서드
    // ==========================

    public List<WeeklyVolumeResponseDto> getWeeklyVolumesForRange(String userId, LocalDate start, LocalDate end) {
        List<WeeklyVolumeDto> rawData = fetchWeeklyVolumes(userId, start, end);
        return fillMissingWeeks(rawData, start, end);
    }

    public MuscleBalanceResponse getMuscleBalance(String userId, LocalDate start, LocalDate endExclusive) {
        User user = userService.findUserOrThrow(userId);

        LocalDate effectiveStart = start != null ? start : LocalDate.now().minusWeeks(4);
        LocalDate effectiveEndEx = endExclusive != null ? endExclusive : LocalDate.now().plusDays(1);

        List<MuscleBalanceDto> rows = analyticsRepository.findMuscleBalance(
                userId,
                effectiveStart.atStartOfDay(),
                effectiveEndEx.atStartOfDay()
        );

        double totalVolume = calculateTotalVolume(rows);
        List<MuscleBalanceResponse.Item> items = buildMuscleBalanceItems(rows, totalVolume);

        return new MuscleBalanceResponse(
                user.getUserName(),
                effectiveStart.toString(),
                effectiveEndEx.toString(),
                totalVolume,
                items
        );
    }

    public List<MaxRepsRow> getRecentMaxRepsByWeight(String userId, Long exerciseTypeId, Double weight, Integer size) {
        validateSize(size);
        validateWeight(weight);
        User user = userService.findUserOrThrow(userId);

        return analyticsRepository.findRecentMaxRepsByWeight(
                user.getUserId(),
                exerciseTypeId,
                weight,
                PageRequest.of(0, size)
        );
    }

    public List<MaxWeightRepsRow> getMaxWeightRepsHistory(String userId, Long exerciseTypeId, Integer size) {
        validateSize(size);
        userService.findUserOrThrow(userId);

        return analyticsNativeRepository.findMaxWeightRepsHistory(userId, exerciseTypeId, size, 0);
    }

    public ExerciseTypeVolumeDto getExerciseTypeVolume(String userId, Long exerciseTypeId, LocalDate start, LocalDate endExclusive) {
        User user = userService.findUserOrThrow(userId);
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = endExclusive != null ? endExclusive.atStartOfDay() : null;

        return analyticsRepository.findExerciseTypeVolume(user.getUserId(), exerciseTypeId, startDateTime, endDateTime);
    }


    // ==========================
    // 낮은 추상화: 내부 로직 / 유틸리티 함수
    // ==========================

    private void validateSize(Integer size) {
        if (size < 1 || size > 30) throw new SizeOutOfRangeException("size는 1이상 30이하만 허용됩니다.");
    }

    private void validateWeight(Double weight) {
        if (weight < 0) throw new IllegalArgumentException("weight는 0 이상이어야 합니다.");
    }

    private List<WeeklyVolumeDto> fetchWeeklyVolumes(String userId, LocalDate start, LocalDate end) {
        return analyticsRepository.findWeeklyVolumeForUserAndRange(userId, start, end);
    }

    private List<WeeklyVolumeResponseDto> fillMissingWeeks(List<WeeklyVolumeDto> raw, LocalDate start, LocalDate end) {
        Map<Integer, Double> volumeMap = raw.stream()
                .collect(Collectors.toMap(WeeklyVolumeDto::getYearWeek, WeeklyVolumeDto::getWeeklyVolume));

        List<WeeklyVolumeResponseDto> result = new ArrayList<>();

        WeekFields wf = WeekFields.ISO;
        LocalDate cursor = start.with(wf.dayOfWeek(), 1);

        while (!cursor.isAfter(end)) {
            int yearWeek = cursor.get(wf.weekBasedYear()) * 100 + cursor.get(wf.weekOfWeekBasedYear());
            double vol = volumeMap.getOrDefault(yearWeek, 0.0);
            result.add(new WeeklyVolumeResponseDto(yearWeek, vol));
            cursor = cursor.plusWeeks(1);
        }

        return result;
    }

    private double calculateTotalVolume(List<MuscleBalanceDto> rows) {
        return rows.stream()
                .mapToDouble(r -> r.getTotalVolume() != null ? r.getTotalVolume() : 0.0)
                .findFirst()
                .orElse(0.0);
    }

    private List<MuscleBalanceResponse.Item> buildMuscleBalanceItems(List<MuscleBalanceDto> rows, double totalVolume) {
        return rows.stream()
                .map(r -> new MuscleBalanceResponse.Item(
                        r.getMuscle(),
                        r.getVolume() != null ? r.getVolume() : 0.0,
                        totalVolume,
                        r.getPct()
                ))
                .toList();
    }
}