package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.WeeklyVolumeChangeDto;
import com.example.fitnessWeb.dto.WeeklyVolumeDto;
import com.example.fitnessWeb.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExerciseAnalyticsService {

    private final ExerciseRecordRepository recordRepo;

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
}