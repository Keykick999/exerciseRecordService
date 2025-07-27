//package com.example.fitnessWeb.service;
//
//import com.example.fitnessWeb.dto.*;
//import com.example.fitnessWeb.entity.*;
//import com.example.fitnessWeb.exception.*;
//import com.example.fitnessWeb.repository.*;
//import org.junit.jupiter.api.*;
//import org.mockito.*;
//import org.springframework.data.domain.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import java.time.LocalDateTime;
//import java.util.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class RoutineServiceTest {
//
//    @Mock private UserRepository userRepository;
//    @Mock private RoutineRepository routineRepository;
//    @Mock private RoutineSetRepository routineSetRepository;
//    @Mock private ExerciseTypeRepository exerciseTypeRepository;
//
//    @InjectMocks private RoutineService routineService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn("testUser");
//        SecurityContext context = mock(SecurityContext.class);
//        when(context.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(context);
//    }
//
//    // saveRoutine
//
//    @Test
//    void saveRoutine_사용자없음_예외() {
//        // given
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
//
//        RoutineRequestDto dto = new RoutineRequestDto();
//        dto.setName("루틴1");
//        dto.setRoutineSets(Collections.emptyList());
//
//        // when & then
//        assertThrows(UserNotFoundException.class, () -> routineService.saveRoutine(dto));
//    }
//
//    @Test
//    void saveRoutine_루틴이름중복_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.existsByNameAndUserUsername("루틴1", "testUser")).thenReturn(true);
//
//        RoutineRequestDto dto = new RoutineRequestDto();
//        dto.setName("루틴1");
//
//        // when & then
//        assertThrows(DuplicateRoutineNameException.class, () -> routineService.saveRoutine(dto));
//    }
//
//    @Test
//    void saveRoutine_유효하지않은세트정보_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.existsByNameAndUserUsername("루틴1", "testUser")).thenReturn(false);
//
//        RoutineSetDto set = new RoutineSetDto();
//        set.setExerciseTypeId(1L);
//        set.setSetNumber(0); // invalid
//        set.setWeight(10); set.setRepetitions(10);
//
//        RoutineRequestDto dto = new RoutineRequestDto();
//        dto.setName("루틴1"); dto.setRoutineSets(List.of(set));
//
//        // when & then
//        assertThrows(InvalidRoutineSetException.class, () -> routineService.saveRoutine(dto));
//    }
//
//    @Test
//    void saveRoutine_세트중복_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.existsByNameAndUserUsername("루틴1", "testUser")).thenReturn(false);
//
//        ExerciseType exerciseType = new ExerciseType(); exerciseType.setId(1L);
//        when(exerciseTypeRepository.findById(1L)).thenReturn(Optional.of(exerciseType));
//
//        RoutineSetDto set1 = new RoutineSetDto(); set1.setExerciseTypeId(1L); set1.setSetNumber(1);
//        RoutineSetDto set2 = new RoutineSetDto(); set2.setExerciseTypeId(1L); set2.setSetNumber(1); // 중복
//
//        RoutineRequestDto dto = new RoutineRequestDto();
//        dto.setName("루틴1"); dto.setRoutineSets(List.of(set1, set2));
//
//        // when & then
//        assertThrows(DuplicateRoutineSetException.class, () -> routineService.saveRoutine(dto));
//    }
//
//
//    @Test
//    void saveRoutine_운동종목없음_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.existsByNameAndUserUsername("루틴1", "testUser")).thenReturn(false);
//
//        RoutineSetDto set = new RoutineSetDto();
//        set.setExerciseTypeId(999L); set.setSetNumber(1); set.setWeight(10); set.setRepetitions(10);
//
//        when(exerciseTypeRepository.findById(999L)).thenReturn(Optional.empty());
//
//        RoutineRequestDto dto = new RoutineRequestDto();
//        dto.setName("루틴1"); dto.setRoutineSets(List.of(set));
//
//        // when & then
//        assertThrows(ExerciseTypeNotFoundException.class, () -> routineService.saveRoutine(dto));
//    }
//
//    @Test
//    void saveRoutine_정상_저장() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.existsByNameAndUserUsername("루틴1", "testUser")).thenReturn(false);
//
//        RoutineSetDto set = new RoutineSetDto();
//        set.setExerciseTypeId(1L); set.setSetNumber(1); set.setWeight(10); set.setRepetitions(10);
//
//        ExerciseType exercise = new ExerciseType(); exercise.setId(1L);
//        when(exerciseTypeRepository.findById(1L)).thenReturn(Optional.of(exercise));
//
//        RoutineRequestDto dto = new RoutineRequestDto();
//        dto.setName("루틴1"); dto.setRoutineSets(List.of(set));
//
//        // when
//        routineService.saveRoutine(dto);
//
//        // then
//        verify(routineRepository).save(any(Routine.class));
//        verify(routineSetRepository).save(any(RoutineSet.class));
//    }
//
//    // getMyRoutines
//
//    @Test
//    void getMyRoutines_사용자없음_예외() {
//        // given
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(UserNotFoundException.class, () -> routineService.getMyRoutines(0, 10));
//    }
//
//    @Test
//    void getMyRoutines_루틴없음_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.findAllByUsername(eq("testUser"), any(Pageable.class)))
//                .thenReturn(Page.empty());
//
//        // when & then
//        assertThrows(RoutineNotFoundException.class, () -> routineService.getMyRoutines(0, 10));
//    }
//
//
//    @Test
//    void getMyRoutines_정상조회() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        RoutineSummaryDto dto = new RoutineSummaryDto(1L, "루틴1", LocalDateTime.now());
//        Page<RoutineSummaryDto> page = new PageImpl<>(List.of(dto));
//
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.findAllByUsername(eq("testUser"), any(Pageable.class)))
//                .thenReturn(page);
//
//        // when
//        Page<RoutineSummaryDto> result = routineService.getMyRoutines(0, 10);
//
//        // then
//        assertEquals(1, result.getTotalElements());
//        assertEquals("루틴1", result.getContent().get(0).getName());
//    }
//
//    // getRoutineDetails
//
//    @Test
//    void getRoutineDetails_사용자없음_예외() {
//        // given
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(UserNotFoundException.class, () -> routineService.getRoutineDetails("루틴1"));
//    }
//
//    @Test
//    void getRoutineDetails_루틴상세없음_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.findRoutineDetailsByNameAndUsername("루틴1", "testUser")).thenReturn(List.of());
//
//        // when & then
//        assertThrows(RoutineNotFoundException.class, () -> routineService.getRoutineDetails("루틴1"));
//    }
//
//    @Test
//    void getRoutineDetails_정상조회() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        RoutineDetailDto detail = new RoutineDetailDto("루틴1", 1L, "벤치프레스", 1, 20.5, 10);
//
//        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
//        when(routineRepository.findRoutineDetailsByNameAndUsername("루틴1", "testUser")).thenReturn(List.of(detail));
//
//        // when
//        List<RoutineDetailDto> result = routineService.getRoutineDetails("루틴1");
//
//        // then
//        assertEquals(1, result.size());
//        assertEquals("벤치프레스", result.get(0).getExerciseType());
//    }
//
//    // updateRoutine
//
//    @Test
//    void updateRoutine_루틴없음_예외() {
//        // given
//        when(routineRepository.findById(1L)).thenReturn(Optional.empty());
//
//        RoutineUpdateRequestDto dto = new RoutineUpdateRequestDto();
//        dto.setRoutineName("새루틴");
//
//        // when & then
//        assertThrows(RoutineNotFoundException.class, () -> routineService.updateRoutine(1L, dto));
//    }
//
//    @Test
//    void updateRoutine_권한없음_예외() {
//        // given
//        User otherUser = new User();
//        otherUser.setUsername("otherUser");
//
//        Routine routine = new Routine();
//        routine.setId(1L);
//        routine.setUser(otherUser);
//
//        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
//
//        RoutineUpdateRequestDto dto = new RoutineUpdateRequestDto();
//        dto.setRoutineName("새루틴");
//
//        // when & then
//        assertThrows(UnauthorizedAccessException.class, () -> routineService.updateRoutine(1L, dto));
//    }
//
//
//    @Test
//    void updateRoutine_유효하지않은운동ID_예외() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        Routine routine = new Routine(); routine.setId(1L); routine.setUser(user); routine.setRoutineSets(new ArrayList<>());
//
//        RoutineSetDto set = new RoutineSetDto();
//        set.setExerciseTypeId(999L); set.setSetNumber(1); set.setWeight(10); set.setRepetitions(10);
//
//        RoutineUpdateRequestDto dto = new RoutineUpdateRequestDto();
//        dto.setRoutineName("새루틴"); dto.setRoutineSets(List.of(set));
//
//        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
//        when(exerciseTypeRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(IllegalArgumentException.class, () -> routineService.updateRoutine(1L, dto));
//    }
//
//
//    @Test
//    void updateRoutine_정상_업데이트() {
//        // given
//        User user = new User(); user.setUsername("testUser");
//        Routine routine = new Routine(); routine.setId(1L); routine.setUser(user); routine.setRoutineSets(new ArrayList<>());
//
//        ExerciseType exercise = new ExerciseType(); exercise.setId(1L);
//        RoutineSetDto set = new RoutineSetDto();
//        set.setExerciseTypeId(1L); set.setSetNumber(1); set.setWeight(15); set.setRepetitions(12);
//
//        RoutineUpdateRequestDto dto = new RoutineUpdateRequestDto();
//        dto.setRoutineName("새루틴"); dto.setRoutineSets(List.of(set));
//
//        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
//        when(exerciseTypeRepository.findById(1L)).thenReturn(Optional.of(exercise));
//
//        // when
//        routineService.updateRoutine(1L, dto);
//
//        // then
//        assertEquals("새루틴", routine.getName());
//        assertEquals(1, routine.getRoutineSets().size());
//    }
//
//    // deleteRoutine
//
//    @Test
//    void deleteRoutine_루틴없음_예외() {
//        // given
//        when(routineRepository.findById(1L)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(RoutineNotFoundException.class, () -> routineService.deleteRoutine(1L));
//    }
//
//    @Test
//    void deleteRoutine_권한없음_예외() {
//        // given
//        User otherUser = new User();
//        otherUser.setUsername("otherUser");
//
//        Routine routine = new Routine();
//        routine.setId(1L);
//        routine.setUser(otherUser);
//
//        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
//
//        // when & then
//        assertThrows(UnauthorizedAccessException.class, () -> routineService.deleteRoutine(1L));
//    }
//
//    @Test
//    void deleteRoutine_정상_삭제() {
//        // given
//        User user = new User();
//        user.setUsername("testUser");
//
//        Routine routine = new Routine();
//        routine.setId(1L);
//        routine.setUser(user);
//
//        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
//
//        // when
//        routineService.deleteRoutine(1L);
//
//        // then
//        verify(routineRepository).delete(routine);
//    }
//}