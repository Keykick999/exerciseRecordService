package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.*;
import com.example.fitnessWeb.entity.ExerciseRecord;
import com.example.fitnessWeb.entity.ExerciseSet;
import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.entity.User;
import com.example.fitnessWeb.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseRecordService {

    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserRepository userRepository;

    // 운동 기록 저장
    @Transactional
    public void saveRecord(String username, ExerciseRecordDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow();

        ExerciseRecord record = ExerciseRecord.builder()
                .date(dto.getDate())
                .user(user)
                .build();
        exerciseRecordRepository.save(record);

        List<ExerciseSet> sets = dto.getSets().stream().map(s ->
                ExerciseSet.builder()
                        .setOrder(s.getSetOrder())
                        .weight(s.getWeight())
                        .reps(s.getReps())
                        .exerciseRecord(record)
                        .exerciseType(exerciseTypeRepository.findById(s.getExerciseTypeId()).orElseThrow())
                        .build()
        ).collect(Collectors.toList());

        record.setSets(sets);
        exerciseRecordRepository.save(record);
    }


    public List<ExerciseRecordResponseDto> getRecords(String username, ExerciseRecordSearchRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow();


        return exerciseRecordRepository.searchRecords(
                user.getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getExerciseName(),
                request.getMuscleGroup()
        );
    }


//    public List<WeeklyMuscleStatDto> getWeeklyMuscleVolumeStats(String username, int weekCount) {
//        User user = userRepository.findByUsername(username).orElseThrow();
//        LocalDate today = LocalDate.now();
//        LocalDate startDate = today.minusWeeks(weekCount - 1).with(DayOfWeek.MONDAY);
//
//        List<ExerciseRecord> records = recordRepository.findByUserAndDateAfter(user, startDate.minusDays(1));
//
//        // 주차별 분할
//        Map<LocalDate, List<ExerciseRecord>> recordsByWeek = records.stream()
//                .collect(Collectors.groupingBy(r ->
//                        r.getDate().with(DayOfWeek.MONDAY)  // 주차 시작일로 묶음
//                ));
//
//        return recordsByWeek.entrySet().stream()
//                .map(entry -> {
//                    LocalDate weekStart = entry.getKey();
//                    List<ExerciseRecord> weeklyRecords = entry.getValue();
//
//                    Map<String, Double> volumeMap = new HashMap<>();
//
//                    for (ExerciseRecord record : weeklyRecords) {
//                        String muscle = record.getExerciseType().getMuscleGroup();
//                        double volume = record.getSets().stream()
//                                .mapToDouble(s -> s.getWeight() * s.getReps())
//                                .sum();
//
//                        volumeMap.put(muscle, volumeMap.getOrDefault(muscle, 0.0) + volume);
//                    }
//
//                    List<MuscleGroupVolumeDto> muscleStats = volumeMap.entrySet().stream()
//                            .map(e -> MuscleGroupVolumeDto.builder()
//                                    .muscleGroup(e.getKey())
//                                    .volume(e.getValue())
//                                    .build())
//                            .toList();
//
//                    return WeeklyMuscleStatDto.builder()
//                            .weekStart(weekStart)
//                            .muscleStats(muscleStats)
//                            .build();
//                })
//                .sorted(Comparator.comparing(WeeklyMuscleStatDto::getWeekStart).reversed())
//                .toList();
//    }
}
