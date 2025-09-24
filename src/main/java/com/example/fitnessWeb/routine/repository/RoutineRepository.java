package com.example.fitnessWeb.routine.repository;

import com.example.fitnessWeb.routine.dto.RoutineDetailDto;
import com.example.fitnessWeb.routine.dto.RoutineSummaryDto;
import com.example.fitnessWeb.routine.domain.Routine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface RoutineRepository extends JpaRepository<Routine, Long> {
    @Query("SELECT new com.example.fitnessWeb.routine.dto.RoutineSummaryDto(r.id, r.name, r.createdAt) " +
            "FROM Routine r WHERE r.user.userId = :userId")
    Page<RoutineSummaryDto> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT new com.example.fitnessWeb.routine.dto.RoutineDetailDto(" +
            "r.name, et.id, et.name, rs.setNumber, rs.weight, rs.repetitions) " +
            "FROM Routine r " +
            "JOIN r.routineSets rs " +
            "JOIN rs.exerciseType et " +
            "WHERE r.name = :routineName AND r.user.userId = :userId")
    List<RoutineDetailDto> findRoutineDetailsByNameAndUserId(@Param("routineName") String routineName,
                                                               @Param("userId") String userId);

    boolean existsByNameAndUserUserId(String name, String userId);

    Object existsByName(String name);
}
