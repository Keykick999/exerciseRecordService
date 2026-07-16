package com.example.fitnessWeb.exerciseRecord.service;

import com.example.fitnessWeb.common.exception.EmptyExerciseSetListException;
import com.example.fitnessWeb.common.exception.ExerciseRecordNotFoundException;
import com.example.fitnessWeb.common.exception.ExerciseTypeNotFoundException;
import com.example.fitnessWeb.common.exception.InvalidRoutineSetException;
import com.example.fitnessWeb.exerciseRecord.domain.ExerciseRecord;
import com.example.fitnessWeb.exerciseRecord.domain.ExerciseSet;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.exerciseRecord.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.exerciseType.domain.ExerciseType;
import com.example.fitnessWeb.exerciseType.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.level.dto.ExerciseSetCommand;
import com.example.fitnessWeb.level.service.LevelService;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseRecordService {

    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final AuthService userService;
    private final LevelService levelService;

    /**
     * 운동 기록 저장
     */
    @Transactional
    public void saveRecord(String userId, List<SaveExerciseSetCommand> saveCommands) {
        User user = userService.findUserOrThrow(userId);
        validateExerciseSetList(saveCommands);

        ExerciseRecord record = createExerciseRecord(user);
        List<ExerciseSet> sets = createExerciseSets(saveCommands, record);

        record.setSets(sets);
        exerciseRecordRepository.save(record);


        List<ExerciseSetCommand> levelCommands = saveCommands.stream()
                        .map(command -> new ExerciseSetCommand(
                                command.weight(),
                                command.reps()
                        ))
                        .toList();

        levelService.processLevel(user, levelCommands);
    }

    /**
     * 운동 기록 전체 조회
     */
    public Slice<ExerciseRecordResponseDto> getRecords(String userId,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate,
                                                       String exerciseName,
                                                       List<String> muscleGroup,
                                                       Pageable pageable) {
        User user = userService.findUserOrThrow(userId);
        return exerciseRecordRepository.searchRecords(user.getUserId(), startDate, endDate, exerciseName, muscleGroup, pageable);
    }

    /**
     * 운동 세트 상세 기록 조회
     */
    public List<ExerciseSetDetailDto> getExerciseSetDetails(Long recordId, String userId) {
        List<ExerciseSetDetailDto> details = exerciseRecordRepository.findExerciseSetDetails(recordId, userId);

        if (details.isEmpty()) {
            throw new ExerciseRecordNotFoundException("운동 기록을 찾을 수 없습니다.");
        }

        return details;
    }

    // ========================= 헬퍼 메서드 (추상화 수준 중간) =========================

    /**
     * ExerciseRecord 생성
     */
    private ExerciseRecord createExerciseRecord(User user) {
        ExerciseRecord record = ExerciseRecord.builder()
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .user(user)
                .build();
        exerciseRecordRepository.save(record);
        return record;
    }

    /**
     * DTO 리스트를 ExerciseSet 리스트로 변환
     */
    private List<ExerciseSet> createExerciseSets(List<SaveExerciseSetCommand> commands, ExerciseRecord record) {
        return commands.stream()
                .map(s -> {
                    validateExerciseSet(s);
                    ExerciseType type = findExerciseTypeOrThrow(s.exerciseTypeId());
                    return buildExerciseSet(s, record, type);
                })
                .collect(Collectors.toList());
    }

    /**
     * ExerciseType 조회
     */
    private ExerciseType findExerciseTypeOrThrow(Long typeId) {
        return exerciseTypeRepository.findById(typeId)
                .orElseThrow(() -> new ExerciseTypeNotFoundException(typeId));
    }

    /**
     * ExerciseSet 생성
     */
    private ExerciseSet buildExerciseSet(SaveExerciseSetCommand s, ExerciseRecord record, ExerciseType type) {
        return ExerciseSet.builder()
                .setOrder(s.setOrder())
                .weight(s.weight())
                .reps(s.reps())
                .exerciseRecord(record)
                .exerciseType(type)
                .build();
    }

    public Optional<LocalDateTime> getLastModified(String userId) {
        return exerciseRecordRepository.findMaxModifiedAtByUserId(userId);
    }

    // ========================= 단일 책임 헬퍼 (추상화 수준 낮음) =========================

    /**
     * DTO 유효성 검사
     */
    private void validateExerciseSetList(List<SaveExerciseSetCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new EmptyExerciseSetListException("운동 세트 목록이 비어있습니다.");
        }
    }

    /**
     * 단일 ExerciseSet 검증
     */
    private void validateExerciseSet(SaveExerciseSetCommand s) {
        if (s.setOrder() <= 0 || s.weight() < 0 || s.reps() < 0) {
            throw new InvalidRoutineSetException("세트 정보가 유효하지 않습니다.");
        }
    }
}