package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.dto.ExerciseSetDto;
import com.example.fitnessWeb.entity.ExerciseRecord;
import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.entity.User;
import com.example.fitnessWeb.exception.*;
import com.example.fitnessWeb.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


class ExerciseRecordServiceTest {

    private ExerciseTypeRepository exerciseTypeRepository;
    private ExerciseRecordRepository exerciseRecordRepository;
    private UserRepository userRepository;
    private ExerciseRecordService exerciseRecordService;

    @BeforeEach
    void setUp() {
        exerciseTypeRepository = mock(ExerciseTypeRepository.class);
        exerciseRecordRepository = mock(ExerciseRecordRepository.class);
        userRepository = mock(UserRepository.class);
        exerciseRecordService = new ExerciseRecordService(exerciseTypeRepository, exerciseRecordRepository, userRepository);
    }

    @Test
    void saveRecord_successfully_saves_data() {
        // given
        String username = "testUser";
        User user = new User();
        user.setId(1L);

        ExerciseType exerciseType = new ExerciseType();
        exerciseType.setId(1L);

        ExerciseSetDto dto = new ExerciseSetDto();
        dto.setExerciseTypeId(1L);
        dto.setReps(10);
        dto.setWeight(50);
        dto.setSetOrder(1);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(exerciseTypeRepository.findById(1L)).thenReturn(Optional.of(exerciseType));

        // when
        exerciseRecordService.saveRecord(username, List.of(dto));

        // then
        verify(exerciseRecordRepository, times(2)).save(any(ExerciseRecord.class));
    }

    @Test
    void saveRecord_throws_when_user_not_found() {
        // given
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> exerciseRecordService.saveRecord("invalid", new ArrayList<>()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void saveRecord_throws_when_empty_list() {
        // given
        String username = "testUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // then
        assertThatThrownBy(() -> exerciseRecordService.saveRecord(username, new ArrayList<>()))
                .isInstanceOf(EmptyExerciseSetListException.class);
    }

    @Test
    void saveRecord_throws_when_invalid_set_data() {
        // given
        String username = "testUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        ExerciseSetDto invalidDto = new ExerciseSetDto();
        invalidDto.setExerciseTypeId(1L);
        invalidDto.setSetOrder(0); // invalid
        invalidDto.setWeight(-1);
        invalidDto.setReps(-1);

        // then
        assertThatThrownBy(() -> exerciseRecordService.saveRecord(username, List.of(invalidDto)))
                .isInstanceOf(InvalidRoutineSetException.class);
    }

    @Test
    void saveRecord_throws_when_exercise_type_not_found() {
        // given
        String username = "testUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));
        when(exerciseTypeRepository.findById(999L)).thenReturn(Optional.empty());

        ExerciseSetDto dto = new ExerciseSetDto();
        dto.setExerciseTypeId(999L);
        dto.setSetOrder(1);
        dto.setReps(10);
        dto.setWeight(30);

        // then
        assertThatThrownBy(() -> exerciseRecordService.saveRecord(username, List.of(dto)))
                .isInstanceOf(ExerciseTypeNotFoundException.class);
    }

    @Test
    @DisplayName("운동 기록 전체 조회 성공")
    void getRecords_success() {
        // given
        String username = "testUser";
        User user = new User();
        user.setId(1L);

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        ExerciseRecordResponseDto record1 = new ExerciseRecordResponseDto(LocalDateTime.now(), "가슴", 1L);
        ExerciseRecordResponseDto record2 = new ExerciseRecordResponseDto(LocalDateTime.now(), "등", 2L);
        Page<ExerciseRecordResponseDto> page = new PageImpl<>(List.of(record1, record2));

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(exerciseRecordRepository.searchRecords(eq(1L), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(page);

        // when
        Page<ExerciseRecordResponseDto> result = exerciseRecordService.getRecords(
                username, start, end, null, null, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getMuscleGroups()).isEqualTo("가슴");
        assertThat(result.getContent().get(1).getMuscleGroups()).isEqualTo("등");
    }

    @Test
    @DisplayName("운동 기록 전체 조회 - 유저 없음 예외 발생")
    void getRecords_userNotFound() {
        // given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);

        // then
        assertThatThrownBy(() -> exerciseRecordService.getRecords(
                "unknown",
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now(),
                null,
                null,
                pageable
        )).isInstanceOf(UserNotFoundException.class); // or UserNotFoundException if thrown
    }


    @Test
    void getExerciseSetDetails_throws_when_record_not_found() {
        when(exerciseRecordRepository.findExerciseSetDetails(anyLong()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> exerciseRecordService.getExerciseSetDetails(1L, "testUser"))
                .isInstanceOf(ExerciseRecordNotFoundException.class);
    }

    @Test
    void getExerciseSetDetails_throws_when_unauthorized_user() {
        ExerciseSetDetailDto detail = new ExerciseSetDetailDto("벤치프레스", 1, 50, 10, "otherUser");
        when(exerciseRecordRepository.findExerciseSetDetails(1L)).thenReturn(List.of(detail));

        assertThatThrownBy(() -> exerciseRecordService.getExerciseSetDetails(1L, "testUser"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getExerciseSetDetails_success() {
        ExerciseSetDetailDto detail = new ExerciseSetDetailDto("벤치프레스", 1, 50, 10, "testUser");
        when(exerciseRecordRepository.findExerciseSetDetails(1L)).thenReturn(List.of(detail));

        List<ExerciseSetDetailDto> result = exerciseRecordService.getExerciseSetDetails(1L, "testUser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("벤치프레스");
    }
}