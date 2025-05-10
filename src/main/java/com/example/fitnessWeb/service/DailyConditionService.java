package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.DailyConditionDto;
import com.example.fitnessWeb.entity.DailyCondition;
import com.example.fitnessWeb.entity.User;
import com.example.fitnessWeb.repository.DailyConditionRepository;
import com.example.fitnessWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyConditionService {

    private final DailyConditionRepository repository;
    private final UserRepository userRepository;

    public void saveOrUpdate(String username, DailyConditionDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow();

        DailyCondition condition = repository.findByUserAndDate(user, dto.getDate())
                .orElse(DailyCondition.builder()
                        .user(user)
                        .date(dto.getDate())
                        .build());

        condition.setWeight(dto.getWeight());
        condition.setMuscleMass(dto.getMuscleMass());
        condition.setConditionLevel(dto.getConditionLevel());
        condition.setNote(dto.getNote());

        repository.save(condition);
    }

    public List<DailyConditionDto> getConditionHistory(String username, LocalDate start, LocalDate end) {
        User user = userRepository.findByUsername(username).orElseThrow();

        return repository.findByUserAndDateBetween(user, start, end).stream()
                .map(c -> {
                    DailyConditionDto dto = new DailyConditionDto();
                    dto.setDate(c.getDate());
                    dto.setWeight(c.getWeight());
                    dto.setMuscleMass(c.getMuscleMass());
                    dto.setConditionLevel(c.getConditionLevel());
                    dto.setNote(c.getNote());
                    return dto;
                })
                .toList();
    }
}
