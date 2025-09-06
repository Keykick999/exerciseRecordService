package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.NutritionEntryCreateRequest;
import com.example.fitnessWeb.dto.NutritionEntryUpdateRequest;
import com.example.fitnessWeb.exception.UserNotFoundException;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.entity.NutritionDay;
import com.example.fitnessWeb.entity.NutritionEntry;
import com.example.fitnessWeb.exception.NutritionNotFoundException;
import com.example.fitnessWeb.exception.UnAuthorizedAccessException;
import com.example.fitnessWeb.repository.NutritionDayRepository;
import com.example.fitnessWeb.repository.NutritionEntryRepository;
import com.example.fitnessWeb.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final UserRepository userRepository;
    private final NutritionEntryRepository entryRepository;
    private final NutritionDayRepository dayRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UserNotFoundException();
        }

        String userId = auth.getName();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }


    // ===== 생성 =====
    @Transactional
    public Long createEntry(NutritionEntryCreateRequest req) {
        User user = getCurrentUser();

        NutritionEntry e = NutritionEntry.builder()
                .user(user)
                .consumedAt(req.getConsumedAt())
                .proteinG(req.getProteinG())
                .carbsG(req.getCarbsG())
                .fatG(req.getFatG())
                .caloriesKcal(req.getCaloriesKcal())
                .deleted(false)
                .build();

        entryRepository.save(e);

        LocalDate date = req.getConsumedAt().toLocalDate();
        applyDeltaToDay(user.getUserId(), date,
                req.getCaloriesKcal(), req.getProteinG(), req.getCarbsG(), req.getFatG());

        return e.getId();
    }

    // ===== 수정 (모든 필드 입력) =====
    @Transactional
    public void updateEntry(Long entryId, NutritionEntryUpdateRequest req) {
        User currentUser = getCurrentUser();

        NutritionEntry e = entryRepository.findById(entryId)
                .orElseThrow(() -> new NutritionNotFoundException());

        if (!e.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnAuthorizedAccessException("user does not have access to endpoint");
        }

        // 이전 스냅샷
        LocalDate oldDate = e.getConsumedAt().toLocalDate();
        BigDecimal oldCal = e.getCaloriesKcal();
        BigDecimal oldPro = e.getProteinG();
        BigDecimal oldCar = e.getCarbsG();
        BigDecimal oldFat = e.getFatG();

        // 신규 값 반영
        LocalDate newDate = req.getConsumedAt().toLocalDate();
        e.setConsumedAt(req.getConsumedAt());
        e.setProteinG(req.getProteinG());
        e.setCarbsG(req.getCarbsG());
        e.setFatG(req.getFatG());
        e.setCaloriesKcal(req.getCaloriesKcal());

        // Day 반영
        if (oldDate.equals(newDate)) {
            BigDecimal dCal = req.getCaloriesKcal().subtract(oldCal);
            BigDecimal dPro = req.getProteinG().subtract(oldPro);
            BigDecimal dCar = req.getCarbsG().subtract(oldCar);
            BigDecimal dFat = req.getFatG().subtract(oldFat);
            if (isNonZero(dCal, dPro, dCar, dFat)) {
                applyDeltaToDay(currentUser.getUserId(), oldDate, dCal, dPro, dCar, dFat);
            }
        } else {
            applyDeltaToDay(currentUser.getUserId(), oldDate, oldCal.negate(), oldPro.negate(), oldCar.negate(), oldFat.negate());
            applyDeltaToDay(currentUser.getUserId(), newDate, req.getCaloriesKcal(), req.getProteinG(), req.getCarbsG(), req.getFatG());
        }
    }

    // ===== 소프트 삭제 =====
    @Transactional
    public void softDeleteEntry(Long entryId) {
        User currentUser = getCurrentUser();

        NutritionEntry e = entryRepository.findById(entryId)
                .orElseThrow(() -> new NutritionNotFoundException());

        if (!e.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnAuthorizedAccessException("user does not have access to endpoint");
        }

        if (!e.isDeleted()) {
            LocalDate date = e.getConsumedAt().toLocalDate();

            applyDeltaToDay(
                    e.getUser().getUserId(),
                    date,
                    e.getCaloriesKcal().negate(),
                    e.getProteinG().negate(),
                    e.getCarbsG().negate(),
                    e.getFatG().negate()
            );

            entryRepository.delete(e); // @SQLDelete → deleted=true, deleted_at=now()
        }
    }

    /** (선택) 특정 날짜 Day 풀 재계산 — 범위(BETWEEN)로 안전하게 합산 */
    @Transactional
    public void recomputeDay(LocalDate date) {
        User currentUser = getCurrentUser();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime endExclusive = date.plusDays(1).atStartOfDay();

        Object[] sums = entryRepository.sumActiveByUserAndConsumedAtBetween(currentUser.getUserId(), start, endExclusive);
        BigDecimal c = (BigDecimal) sums[0];
        BigDecimal p = (BigDecimal) sums[1];
        BigDecimal cb = (BigDecimal) sums[2];
        BigDecimal f = (BigDecimal) sums[3];

        NutritionDay day = dayRepository.findByUser_UserIdAndDate(currentUser.getUserId(), date)
                .orElseGet(() -> NutritionDay.builder()
                        .user(userRepository.getReferenceById(currentUser.getUserId()))
                        .date(date)
                        .caloriesKcal(BigDecimal.ZERO)
                        .proteinG(BigDecimal.ZERO)
                        .carbsG(BigDecimal.ZERO)
                        .fatG(BigDecimal.ZERO)
                        .build());

        day.setCaloriesKcal(c);
        day.setProteinG(p);
        day.setCarbsG(cb);
        day.setFatG(f);

        dayRepository.save(day);
    }

    /** Day 증감 반영 (+ 낙관적 락 재시도) */
    private void applyDeltaToDay(String userId, LocalDate date,
                                 BigDecimal dCal, BigDecimal dPro, BigDecimal dCar, BigDecimal dFat) {
        int maxRetry = 3;
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                NutritionDay day = dayRepository.findByUser_UserIdAndDate(userId, date)
                        .orElseGet(() -> NutritionDay.builder()
                                .user(userRepository.getReferenceById(userId))
                                .date(date)
                                .caloriesKcal(BigDecimal.ZERO)
                                .proteinG(BigDecimal.ZERO)
                                .carbsG(BigDecimal.ZERO)
                                .fatG(BigDecimal.ZERO)
                                .build());

                day.setCaloriesKcal(day.getCaloriesKcal().add(dCal));
                day.setProteinG(day.getProteinG().add(dPro));
                day.setCarbsG(day.getCarbsG().add(dCar));
                day.setFatG(day.getFatG().add(dFat));

                dayRepository.saveAndFlush(day); // @Version 체크
                return;
            } catch (OptimisticLockException ex) {
                if (attempt == maxRetry) throw ex;
                // 필요 시 짧은 backoff 추가
            }
        }
    }

    private boolean isNonZero(BigDecimal... nums) {
        for (BigDecimal n : nums) {
            if (n != null && n.compareTo(BigDecimal.ZERO) != 0) return true;
        }
        return false;
    }
}