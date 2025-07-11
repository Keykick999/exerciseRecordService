package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.RoutineDetailDto;
import com.example.fitnessWeb.dto.RoutineSummaryDto;
import com.example.fitnessWeb.entity.Routine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RoutineRepository extends JpaRepository<Routine, Long> {
    @Query("SELECT new com.example.fitnessWeb.dto.RoutineSummaryDto(r.id, r.name, r.createdAt) " +
            "FROM Routine r WHERE r.user.username = :username")
    Page<RoutineSummaryDto> findAllByUsername(String username, Pageable pageable);

    @Query("SELECT new com.example.fitnessWeb.dto.RoutineDetailDto(" +
            "r.name, et.id, et.name, rs.setNumber, rs.weight, rs.repetitions) " +
            "FROM Routine r " +
            "JOIN r.routineSets rs " +
            "JOIN rs.exerciseType et " +
            "WHERE r.name = :routineName AND r.user.username = :username")
    List<RoutineDetailDto> findRoutineDetailsByNameAndUsername(@Param("routineName") String routineName,
                                                               @Param("username") String username);

    boolean existsByNameAndUserUsername(String name, String username);

    Object existsByName(String name);
}
