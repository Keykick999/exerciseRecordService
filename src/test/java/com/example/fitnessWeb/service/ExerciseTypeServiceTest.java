//package com.example.fitnessWeb.service;
//
//import com.example.fitnessWeb.entity.ExerciseType;
//import com.example.fitnessWeb.entity.ExerciseTypeDocument;
//import com.example.fitnessWeb.repository.ExerciseSearchTermRepository;
//import com.example.fitnessWeb.repository.ExerciseTypeRepository;
//import com.example.fitnessWeb.repository.ExerciseTypeSearchRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.springframework.data.domain.*;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//class ExerciseTypeServiceTest {
//
//    @Mock private ExerciseTypeRepository exerciseTypeRepository;
//    @Mock private ExerciseSearchTermRepository exerciseSearchTermRepository;
//    @Mock private ExerciseTypeSearchRepository exerciseTypeSearchRepository;
//
//    @InjectMocks private ExerciseTypeService exerciseTypeService;
//
//    @Test
//    void 검색어없고_머슬그룹없으면_전체조회() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<ExerciseType> expectedPage = new PageImpl<>(List.of(), pageable, 0);
//        when(exerciseTypeRepository.findAll(pageable)).thenReturn(expectedPage);
//
//        // when
//        Page<ExerciseType> result = exerciseTypeService.searchExercise("", null, 0, 10);
//
//        // then
//        verify(exerciseTypeRepository).findAll(pageable);
//        assertEquals(expectedPage, result);
//    }
//
//
//    @Test
//    void 검색어없고_머슬그룹있으면_머슬그룹기준조회() {
//        // given
//        HashSet<String> groups = new HashSet<>(Set.of("가슴", "등"));
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<ExerciseType> expectedPage = new PageImpl<>(List.of(), pageable, 0);
//        when(exerciseTypeRepository.findByMuscleGroups(groups, pageable)).thenReturn(expectedPage);
//
//        // when
//        Page<ExerciseType> result = exerciseTypeService.searchExercise("", groups, 0, 10);
//
//        // then
//        verify(exerciseTypeRepository).findByMuscleGroups(groups, pageable);
//        assertEquals(expectedPage, result);
//    }
//
//    @Test
//    void 검색어있고_머슬그룹없으면_ES이름검색() {
//        // given
//        String keyword = "벤치";
//        ExerciseTypeDocument doc = ExerciseTypeDocument.builder()
//                .id("1").name("벤치프레스").muscleGroup("가슴").build();
//        when(exerciseTypeSearchRepository.findByNameContaining(keyword))
//                .thenReturn(List.of(doc));
//
//        // when
//        Page<ExerciseType> result = exerciseTypeService.searchExercise(keyword, null, 0, 10);
//
//        // then
//        assertEquals(1, result.getContent().size());
//        assertEquals("벤치프레스", result.getContent().get(0).getName());
//        assertEquals("가슴", result.getContent().get(0).getMuscleGroup());
//    }
//
//    @Test
//    void 검색어있고_머슬그룹있으면_ES검색후_그룹필터링() {
//        // given
//        String keyword = "랫";
//        HashSet<String> muscleGroups = new HashSet<>(Set.of("등"));
//
//        ExerciseTypeDocument doc1 = ExerciseTypeDocument.builder()
//                .id("1").name("랫풀다운").muscleGroup("등").build();
//        ExerciseTypeDocument doc2 = ExerciseTypeDocument.builder()
//                .id("2").name("랫익스텐션").muscleGroup("하체").build(); // 필터링 대상
//
//        when(exerciseTypeSearchRepository.findByNameContaining(keyword))
//                .thenReturn(List.of(doc1, doc2));
//
//        // when
//        Page<ExerciseType> result = exerciseTypeService.searchExercise(keyword, muscleGroups, 0, 10);
//
//        // then
//        assertEquals(1, result.getContent().size());
//        assertEquals("랫풀다운", result.getContent().get(0).getName());
//        assertEquals("등", result.getContent().get(0).getMuscleGroup());
//    }
//
//    @Test
//    void 검색결과없음_빈리스트반환() {
//        // given
//        String keyword = "없는운동";
//        when(exerciseTypeSearchRepository.findByNameContaining(keyword))
//                .thenReturn(List.of());
//
//        // when
//        Page<ExerciseType> result = exerciseTypeService.searchExercise(keyword, null, 0, 10);
//
//        // then
//        assertTrue(result.isEmpty());
//    }
//}