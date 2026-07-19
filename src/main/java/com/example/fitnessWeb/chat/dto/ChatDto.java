package com.example.fitnessWeb.chat.dto;

import java.time.LocalDateTime;

public record ChatDto(Long id, String title, LocalDateTime createdAt, LocalDateTime lastAccessedAt) {}