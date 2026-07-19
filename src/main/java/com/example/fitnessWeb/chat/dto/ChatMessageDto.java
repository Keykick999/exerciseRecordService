package com.example.fitnessWeb.chat.dto;

import com.example.fitnessWeb.chat.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

public record ChatMessageDto(Long id, Role role, String content, LocalDateTime createdAt, Integer orderNo) {}
