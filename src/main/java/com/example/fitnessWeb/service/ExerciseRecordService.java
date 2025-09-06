package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.*;
import com.example.fitnessWeb.entity.ExerciseRecord;
import com.example.fitnessWeb.entity.ExerciseSet;
import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.exception.*;
import com.example.fitnessWeb.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExerciseRecordService {

    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserRepository userRepository;

    // 운동 기록 저장
    @Transactional
    public void saveRecord(String userId, List<ExerciseSetDto> dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (dto == null || dto.isEmpty()) {
            throw new EmptyExerciseSetListException("운동 세트 목록이 비어있습니다.");
        }

        ExerciseRecord record = ExerciseRecord.builder()
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        exerciseRecordRepository.save(record);

        List<ExerciseSet> sets = dto.stream().map(s -> {
            if (s.getSetOrder() <= 0 || s.getWeight() < 0 || s.getReps() < 0) {
                throw new InvalidRoutineSetException("세트 정보가 유효하지 않습니다.");
            }

            ExerciseType exerciseType = exerciseTypeRepository.findById(s.getExerciseTypeId())
                    .orElseThrow(() -> new ExerciseTypeNotFoundException(s.getExerciseTypeId()));

            return ExerciseSet.builder()
                    .setOrder(s.getSetOrder())
                    .weight(s.getWeight())
                    .reps(s.getReps())
                    .exerciseRecord(record)
                    .exerciseType(exerciseType)
                    .build();
        }).collect(Collectors.toList());

        record.setSets(sets);
        exerciseRecordRepository.save(record);
    }

    // 운동 기록 전체 조회
    public Page<ExerciseRecordResponseDto> getRecords(String userId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      String exerciseName,
                                                      List<String> muscleGroup,
                                                      Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        return exerciseRecordRepository.searchRecords(user.getUserId(), startDate, endDate, exerciseName, muscleGroup, pageable);
    }

    // 운동 상세 기록 조회
    public List<ExerciseSetDetailDto> getExerciseSetDetails(Long recordId, String userId) {
        List<ExerciseSetDetailDto> details = exerciseRecordRepository.findExerciseSetDetails(recordId, userId);

        if (details.isEmpty()) {
            throw new ExerciseRecordNotFoundException("운동 기록을 찾을 수 없습니다.");
        }

        return details;
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
