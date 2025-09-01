package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.MuscleBalanceResponse;
import com.example.fitnessWeb.dto.WeeklyVolumeChangeDto;
import com.example.fitnessWeb.dto.WeeklyVolumeDto;
import com.example.fitnessWeb.projection.MuscleBalanceRow;
import com.example.fitnessWeb.repository.AnalyticsRepository;
import com.example.fitnessWeb.repository.ExerciseRecordRepository;
import com.example.fitnessWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExerciseAnalyticsService {

    private final ExerciseRecordRepository recordRepo;

    private final AnalyticsRepository analyticsRepository;

    private final UserRepository userRepository;


    /**
     * 사용자별 주간 부하(+변화율) 리스트를 반환.
     */
    public List<WeeklyVolumeChangeDto> getWeeklyVolumeChanges() {
        List<WeeklyVolumeDto> raw = recordRepo.findAllWeeklyVolumes();

        List<WeeklyVolumeChangeDto> result = new ArrayList<>();
        Map<String, Double> prevVolumeByUser = new HashMap<>();

        for (WeeklyVolumeDto w : raw) {
            String user = w.getUsername();
            double current = w.getWeeklyVolume();

            Double prev = prevVolumeByUser.get(user);
            double pct = 0;
            if (prev != null && prev != 0) {
                pct = (current - prev) / prev * 100.0;
            }
            // 소수점 2자리
            pct = Math.round(pct * 100.0) / 100.0;

            result.add(WeeklyVolumeChangeDto.builder()
                    .username(user)
                    .yearWeek(w.getYearWeek())
                    .weeklyVolume(current)
                    .volumeChangePct(pct)
                    .build()
            );

            // 이번 주치를 다음 주 비교용으로 저장
            prevVolumeByUser.put(user, current);
        }

        return result;
    }


    public MuscleBalanceResponse getMuscleBalance(String username,
                                                  LocalDate start,
                                                  LocalDate endExclusive) {
        LocalDate effectiveStart = (start != null) ? start : LocalDate.now().minusWeeks(4);
        LocalDate effectiveEndEx = (endExclusive != null) ? endExclusive : LocalDate.now().plusDays(1);

        LocalDateTime s = effectiveStart.atStartOfDay();
        LocalDateTime e = effectiveEndEx.atStartOfDay();

        List<MuscleBalanceRow> rows = analyticsRepository.findMuscleBalance(username, s, e);

        long total = rows.stream()
                .map(r -> r.getTotalVolume() == null ? 0L : r.getTotalVolume())
                .findFirst().orElse(0L);

        var items = rows.stream()
                .map(r -> new MuscleBalanceResponse.Item(
                        r.getMuscle(),
                        r.getVolume() == null ? 0L : r.getVolume(),
                        total,
                        r.getPct()
                ))
                .toList();

        return new MuscleBalanceResponse(
                username,
                effectiveStart.toString(),
                effectiveEndEx.toString(),
                total,
                items
        );
    }
}