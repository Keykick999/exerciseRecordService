package com.example.fitnessWeb.entity;

import com.example.fitnessWeb.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "nutrition_day",
        uniqueConstraints = @UniqueConstraint(name = "ux_day_user_date", columnNames = {"user_id", "date"}),
        indexes = {
                @Index(name = "idx_day_user_date", columnList = "user_id, date")
        }
)
public class NutritionDay extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: users.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 사용자 로컬 날짜 버킷 */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /** 하루 합계 값들 */
    @Column(name = "calories_kcal", nullable = false, precision = 9, scale = 1)
    private BigDecimal caloriesKcal;

    @Column(name = "protein_g", nullable = false, precision = 9, scale = 1)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", nullable = false, precision = 9, scale = 1)
    private BigDecimal carbsG;

    @Column(name = "fat_g", nullable = false, precision = 9, scale = 1)
    private BigDecimal fatG;

    /** 동시성 제어용 */
    @Version
    private Long version;
}