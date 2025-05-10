package com.example.fitnessWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private double weight;         // 체중 (kg)

    private double muscleMass;     // 근육량 (kg)

    private int conditionLevel;    // 컨디션 점수 (1~10)

    private String note;           // 메모 (optional)

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
