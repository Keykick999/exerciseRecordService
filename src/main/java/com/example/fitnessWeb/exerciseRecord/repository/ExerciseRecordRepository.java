package com.example.fitnessWeb.exerciseRecord.repository;

import com.example.fitnessWeb.exerciseRecord.domain.ExerciseRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long>, ExerciseRecordRepositoryCustom {
    // 특정 사용자의 운동 기록 중 가장 최근 수정 시각 조회
    @Query("SELECT MAX(e.modifiedAt) FROM ExerciseRecord e WHERE e.user.userId = :userId")
    Optional<LocalDateTime> findMaxModifiedAtByUserId(String userId);
}
