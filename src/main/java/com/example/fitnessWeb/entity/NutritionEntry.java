package com.example.fitnessWeb.entity;

import com.example.fitnessWeb.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "nutrition_entry",
        indexes = {
                @Index(name = "idx_entry_user_datetime", columnList = "user_id, consumed_at")
        }
)
@SQLDelete(sql = "UPDATE nutrition_entry SET deleted = true, deleted_at = now() WHERE id = ?")
@Where(clause = "deleted = false")
public class NutritionEntry extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: users.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 섭취 시각(로컬) — 집계는 서비스/리포지토리에서 날짜 파생해 사용 */
    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    /** 영양 성분 */
    @Column(name = "protein_g", nullable = false, precision = 9, scale = 1)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", nullable = false, precision = 9, scale = 1)
    private BigDecimal carbsG;

    @Column(name = "fat_g", nullable = false, precision = 9, scale = 1)
    private BigDecimal fatG;

    @Column(name = "calories_kcal", nullable = false, precision = 9, scale = 1)
    private BigDecimal caloriesKcal;

    /** 소프트 삭제 */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}